package com.sos.scheduler.engine.agent.task

import com.sos.scheduler.engine.agent.command.CommandMeta
import com.sos.scheduler.engine.agent.configuration.AgentConfiguration
import com.sos.scheduler.engine.agent.data.AgentTaskId
import com.sos.scheduler.engine.agent.data.commandresponses.{EmptyResponse, Response, StartTaskFailed, StartTaskResponse, StartTaskSucceeded}
import com.sos.scheduler.engine.agent.data.commands._
import com.sos.scheduler.engine.agent.data.views.{TaskHandlerOverview, TaskHandlerView, TaskOverview}
import com.sos.scheduler.engine.agent.task.TaskHandler._
import com.sos.scheduler.engine.base.exceptions.StandardPublicException
import com.sos.scheduler.engine.base.process.ProcessSignal
import com.sos.scheduler.engine.base.process.ProcessSignal.{SIGKILL, SIGTERM}
import com.sos.scheduler.engine.common.log.LazyScalaLogger.AsLazyScalaLogger
import com.sos.scheduler.engine.common.scalautil.AutoClosing.closeOnError
import com.sos.scheduler.engine.common.scalautil.Logger
import com.sos.scheduler.engine.common.soslicense.LicenseKeyException
import com.sos.scheduler.engine.common.system.OperatingSystem.isWindows
import com.sos.scheduler.engine.common.time.ScalaTime._
import com.sos.scheduler.engine.common.time.timer.TimerService
import com.sos.scheduler.engine.common.utils.ConcurrentRegister
import com.sos.scheduler.engine.common.utils.Exceptions.ignoreException
import java.time.Instant
import java.time.Instant.now
import java.util.NoSuchElementException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

/**
 * @author Joacim Zschimmer
 */
@Singleton
final class TaskHandler @Inject private(
  newAgentTask: AgentTaskFactory,
  agentConfiguration: AgentConfiguration,
  timerService: TimerService)
  (implicit ec: ExecutionContext)
extends TaskHandlerView {

  private val terminating = new AtomicBoolean
  private val terminatedPromise = Promise[Unit]()
  private val tasks = new TaskRegister
  private val crashKillScriptOption = for (script ← agentConfiguration.killScript if agentConfiguration.crashKillScriptEnabled)
    yield new CrashKillScript(script, agentConfiguration.crashKillScriptFile)
  private val licensedTaskLimiter = new LicensedTaskLimiter {
    def errorMessage(taskCount: Int) = "No license key provided by master to execute jobs in parallel " +
      s"($taskCount unreleased tasks: ${ (tasks.values filterNot { _.terminated.isCompleted } mkString ", ") })"
  }

  def isTerminating = terminating.get
  def terminated = terminatedPromise.future

  def execute(command: Command, meta: CommandMeta = CommandMeta()): Future[Response] =
    command match {
      case o: StartTask ⇒ checkedStartTask(o, meta)
      case CloseTask(id, kill) ⇒ executeCloseTask(id, kill)
      case SendProcessSignal(id, signal) ⇒ executeSendProcessSignal(id, signal)
      case o: Terminate ⇒ executeTerminate(o)
      case AbortImmediately ⇒ executeAbortImmediately()
    }

  private def checkedStartTask(command: StartTask, meta: CommandMeta): Future[StartTaskResponse] =
    Future {
      checkPreconditions(meta)  // Exception is here is like an HTTP exception, leading to Master's Agent bunch fail-over (trying another Agent)
      try {
        val response = executeStartTask(command, meta)  // Exception is here is return as StartTaskFailed, leading to Master Task failure
        if (command.isLegacy) response.toLegacy else response  // May throw an exception
      } catch {
        case t: LicenseKeyException ⇒
          throw t  // HTTP error, should trigger agent bunch fail-over
        case NonFatal(t) ⇒
          logger.debug(t.toString, t)
          StartTaskFailed(t.toString)
      }
    }

  private def checkPreconditions(meta: CommandMeta): Unit = {
    if (isTerminating) throw new StandardPublicException("Agent is terminating and does no longer accept task starts")
  }

  private def executeStartTask(command: StartTask, meta: CommandMeta): StartTaskResponse = {
    val (removeLicensedTask, task) =
      licensedTaskLimiter.countTask(meta.licenseKeyBunch) {
        val task = newAgentTask(command, meta.clientIpOption)
        for (o ← crashKillScriptOption) o.add(task.id, task.pidOption, task.startMeta.taskId, task.startMeta.job)
        tasks.insert(task)
        closeOnError(task) {
          task.start()
        }
        task.onTunnelInactivity(killAfterTunnelInactivity(task))
        task
      }
    task.taskReleaseFuture onComplete { _ ⇒ removeLicensedTask() }
    task.terminated onComplete { _ ⇒ removeLicensedTask() }   // Fallback, to be sure that free license is released
    StartTaskSucceeded(task.id, task.tunnelToken)
  }

  private def killAfterTunnelInactivity(task: AgentTask)(since: Instant): Unit = {
    logger.error(s"$task has no connection activity since $since. Task is being killed")
    ignoreException(logger.asLazy.error) { task.sendProcessSignal(SIGKILL) }
    task.closeTunnel()  // This terminates Remoting and then StandardTaskServer
    task.terminated.onComplete { _ ⇒ removeTaskAfterTermination(task) }
  }

  private def executeCloseTask(id: AgentTaskId, kill: Boolean) = {
    val task = tasks(id)
    if (kill) tryKillTask(task)
    task.terminated recover {
      case t ⇒ logger.error(s"$task: $t", t)
    } map { _ ⇒
      task.deleteLogFiles()
      // Now, the master has completed all API calls or the connection has been closed
      removeTaskAfterTermination(task)
      EmptyResponse
    }
  }

  private def tryKillTask(task: AgentTask): Unit =
    ignoreException(logger.asLazy.error) {
      task.sendProcessSignal(SIGKILL)
    }

  private def removeTaskAfterTermination(task: AgentTask): Unit = {
    logger.info(s"$task terminated")
    task.close()
    tasks -= task.id
    for (o ← crashKillScriptOption) o.remove(task.id)
  }

  private def executeSendProcessSignal(id: AgentTaskId, signal: ProcessSignal) = Future {
    tasks(id).sendProcessSignal(signal)
    EmptyResponse
  }

  private def executeTerminate(command: Terminate) = Future {
    if (terminating.getAndSet(true)) throw new StandardPublicException("Agent is already terminating")
    if (command.sigtermProcesses) {
      trySigtermProcesses()
    }
    for (t ← command.sigkillProcessesAfter if t < Terminate.MaxDuration) {
      sigkillProcessesAt(now() + t)
    }
    terminateWithTasksNotBefore(now() + ImmediateTerminationDelay)
    EmptyResponse
  }

  private def trySigtermProcesses(): Unit =
    if (isWindows) {
      logger.debug("Terminate: Under Windows, SIGTERM is ignored")
    } else {
      sendSignalToAllProcesses(SIGTERM)
    }

  private def sigkillProcessesAt(at: Instant): Unit = {
    logger.info(s"All task processes will be terminated with SIGKILL at $at")
    timerService.at(at, "SIGKILL all processes") onElapsed {
      sendSignalToAllProcesses(SIGKILL)
    }
  }

  private def sendSignalToAllProcesses(signal: ProcessSignal): Unit =
    for (p ← tasks) {
      logger.warn(s"$signal $p")
      ignoreException(logger.asLazy.warn) { p.sendProcessSignal(signal) }
    }

  private def terminateWithTasksNotBefore(notBefore: Instant): Unit = {
    Future.sequence(tasks map { _.terminated }) onComplete { o ⇒
      val delay = notBefore - now()
      if (delay > 0.s) {
        // Wait until HTTP request with termination command probably has been responded
        logger.debug(s"Delaying termination for ${delay.pretty}")
      }
      timerService.delay(delay, "Terminate after HTTP response has been sent") onElapsed {
        logger.info("Agent is terminating now")
        terminatedPromise.complete(o map { _ ⇒ () })
      }
    }
  }

  private def executeAbortImmediately(): Nothing = {
    for (o ← tasks) ignoreException(logger.asLazy.warn) { o.sendProcessSignal(SIGKILL) }
    val msg = "Due to command AbortImmediately, Agent is halted now!"
    logger.warn(msg)
    System.err.println(msg)
    Runtime.getRuntime.halt(1)
    throw new Error("halt")
  }

  def overview = TaskHandlerOverview(
    currentTaskCount = tasks.size,
    totalTaskCount = tasks.totalCount)

  def taskOverviews: immutable.Seq[TaskOverview] = (tasks map { _.overview }).toVector

  def taskOverview(id: AgentTaskId) = tasks(id).overview
}

private object TaskHandler {
  private val logger = Logger(getClass)
  private val ImmediateTerminationDelay = 1.s  // Allow HTTP with termination command request to be responded

  private class TaskRegister extends ConcurrentRegister[AgentTask] {
    override def onAdded(task: AgentTask) = logger.info(s"$task registered")
    override def onRemoved(task: AgentTask) = logger.info(s"$task unregistered")
    override protected def throwNoSuchKey(id: AgentTask#Key) = throw new UnknownTaskException(id)
  }

  final class UnknownTaskException(agentTaskId: AgentTaskId) extends NoSuchElementException {
    override def getMessage = s"Unknown $agentTaskId"
  }
}
