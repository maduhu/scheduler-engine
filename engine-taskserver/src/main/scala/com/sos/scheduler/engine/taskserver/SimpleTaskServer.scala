package com.sos.scheduler.engine.taskserver

import com.sos.scheduler.engine.common.scalautil.AutoClosing.autoClosing
import com.sos.scheduler.engine.common.scalautil.Closers.implicits._
import com.sos.scheduler.engine.common.scalautil.{HasCloser, Logger}
import com.sos.scheduler.engine.minicom.remoting.Remoting
import com.sos.scheduler.engine.taskserver.SimpleTaskServer._
import com.sos.scheduler.engine.taskserver.spoolerapi.{ProxySpoolerLog, ProxySpoolerTask}
import com.sos.scheduler.engine.taskserver.task.{RemoteModuleInstanceServer, TaskStartArguments}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * A blocking [[TaskServer]], running in a own thread.
 *
 * @author Joacim Zschimmer
 */
final class SimpleTaskServer(startArguments: TaskStartArguments) extends TaskServer with HasCloser {

  private val controllingScheduler = new TcpConnection(startArguments.controllerInetSocketAddress).closeWithCloser
  private val remoting = new Remoting(controllingScheduler, IDispatchFactories, ProxyIDispatchFactories)

  private val terminatedPromise = Promise[Unit]()
  def terminated = terminatedPromise.future

  def start(): Unit =
    Future {
      blocking {
        controllingScheduler.connect()
        try remoting.run()
        catch {
          case t: Throwable ⇒
            logger.error(t.toString, t)
            throw t
        }
      }
    } onComplete { o ⇒
      terminatedPromise.complete(o)
    }

  def kill(): Unit = controllingScheduler.close()

  override def toString = s"TaskServer(controller=${startArguments.controllerAddress})"
}

object SimpleTaskServer {
  private val IDispatchFactories = List(RemoteModuleInstanceServer)
  private val ProxyIDispatchFactories = List(ProxySpoolerLog, ProxySpoolerTask)
  private val logger = Logger(getClass)

  def run(conf: TaskStartArguments): Unit =
    autoClosing(new SimpleTaskServer(conf)) { taskServer ⇒
      taskServer.start()
      Await.result(taskServer.terminated, Duration.Inf)
    }
}