package com.sos.scheduler.engine.tests.scheduler.job.jobmethods

import com.sos.scheduler.engine.common.scalautil.ScalazStyle._
import com.sos.scheduler.engine.agent.test.AgentTest
import com.sos.scheduler.engine.common.utils.FreeTcpPortFinder.findRandomFreeTcpPort
import com.sos.scheduler.engine.data.job.JobPath
import com.sos.scheduler.engine.data.message.MessageCode
import com.sos.scheduler.engine.test.SchedulerTestUtils._
import com.sos.scheduler.engine.test.configuration.TestConfiguration
import com.sos.scheduler.engine.test.scalatest.ScalaSchedulerTest
import com.sos.scheduler.engine.tests.scheduler.job.jobmethods.JobMethodsIT._
import com.sos.scheduler.engine.tests.scheduler.job.jobmethods.TestJob._
import com.sos.scheduler.engine.tests.scheduler.job.jobmethods.TestMonitor._
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner

/**
 * @author Joacim Zschimmer
 */
@RunWith(classOf[JUnitRunner])
final class JobMethodsIT extends FreeSpec with ScalaSchedulerTest with AgentTest {

  private lazy val tcpPort = findRandomFreeTcpPort()
  override protected lazy val testConfiguration = TestConfiguration(getClass, mainArguments = List(s"-tcp-port=$tcpPort"))

  "Self test" in {
    val lines = """xx
      >first< CALLED
      xx
      xx >second< CALLED yy"""
    assert(calls(lines) == List("first", "second"))
  }

  "Method call sequence after a job method return false" - {
    List[(Map[String, String], List[String])](
      Map(SpoolerInitName → False) → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      Map(SpoolerOpenName → False) → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerCloseName,
        SpoolerOnSuccessName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      Map(SpoolerProcessName → False) → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnSuccessName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      Map(SpoolerProcessName → False, SpoolerProcessAfterName → InvertResult) → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnSuccessName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      Map[String, String]() → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnSuccessName,
        SpoolerExitName,
        SpoolerTaskAfterName)
    ) foreach { case (methodReturns, expectedCalls) ⇒
      s"$methodReturns" - {
        check(methodReturns, expectedCalls)
      }
    }
  }

  "Method call sequence after a method throws an exception" - {
    List(
      SpoolerInitName → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      SpoolerOpenName → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerCloseName,
        SpoolerOnErrorName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      SpoolerProcessName → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnErrorName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      SpoolerCloseName → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnErrorName,
        SpoolerExitName,
        SpoolerTaskAfterName),
      SpoolerExitName → List(
        SpoolerTaskBeforeName,
        SpoolerInitName,
        SpoolerOpenName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerProcessBeforeName,
        SpoolerProcessName,
        SpoolerProcessAfterName,
        SpoolerCloseName,
        SpoolerOnSuccessName,
        SpoolerExitName,
        SpoolerTaskAfterName)
    ) foreach { case (failingMethod, expectedCalls) ⇒
      s"Error in $failingMethod" - {
        check(Map(failingMethod → Error), expectedCalls, toleratedErrorCodes = Set(MessageCode("COM-80020009"), MessageCode("Z-JAVA-105")))
      }
    }
  }

  private def check(methodReturns: Map[String, String], expectedCallsIncludingMonitor: Iterable[String], toleratedErrorCodes: Set[MessageCode] = Set()): Unit =
    for ((withMonitor, monitorTestGroupName) ← List(false → "Without monitor", true → "With monitor")) {
      val expectedCalls = expectedCallsIncludingMonitor filterNot { name ⇒ !withMonitor && TestMonitor.AllMethodNames(name) } // Monitor-Methoden nur erwarten, wenn der Job einen Monitor hat
      if (!withMonitor && (methodReturns.keys exists TestMonitor.AllMethodNames)) {
        s"$monitorTestGroupName => (not applicable)" in {}
      } else {
        s"$monitorTestGroupName => ${expectedCalls mkString ", "}" - {
          for (language ← List("java", "javascript");
               agentMode ← List("", "agent")) {
            val jobName = List(language, agentMode) ++ withMonitor.option("monitor") filter { _.nonEmpty } mkString("test-", "-", "")
            jobName in {
              val taskResult = controller.toleratingErrorCodes(toleratedErrorCodes) {
                runJobAndWaitForEnd(JobPath.makeAbsolute(jobName), variables = MethodDefaults.toMap ++ methodReturns)
              }
              assert(calls(taskResult.logString) == expectedCalls)
            }
          }
        }
      }
    }

  /** Selects all lines matching `CalledPattern` and returns a list of the names. */
  private def calls(logString: String): List[String] = {
    val r = for (line ← logString split "\n";
                 content ← (CalledPattern findFirstMatchIn line) map { _.group(1) })
            yield content
    r.toList
  }
}

object JobMethodsIT {
  private val False = false.toString
  private val CalledPattern = ">([a-z_]+)< CALLED".r
  private val Error = "ERROR"  // lets .toBoolean fail
  private val MethodDefaults: Map[String, String] = ((TestJob.AllMethodNames ++ TestMonitor.AllMethodNames) map { _ → true.toString }).toMap ++
      Set(SpoolerProcessAfterName → KeepResult)
}