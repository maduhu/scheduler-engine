package com.sos.scheduler.engine.taskserver.task.process

import com.sos.scheduler.engine.common.scalautil.Logger
import com.sos.scheduler.engine.common.time.Stopwatch
import com.sos.scheduler.engine.data.job.ResultCode
import com.sos.scheduler.engine.taskserver.task.process.JavaProcessTest._
import java.lang.System.{err, exit, out}
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * @author Joacim Zschimmer
 */
@RunWith(classOf[JUnitRunner])
final class JavaProcessTest extends FreeSpec {
  "JavaProcess" in {
    val lines = mutable.Buffer[String]()
    val stopwatch = new Stopwatch
    val process = JavaProcess.startJava(
      options = List("-Xmx10m", s"-Dtest=$TestValue"),
      classpath = Some(JavaProcess.OwnClasspath),
      mainClass = JavaProcessTest.getClass.getName stripSuffix "$",   // Scala object class name ends with '$'
      arguments = Arguments)
    try {
      val returnCode = process.waitForTermination(lines += _)
      logger.error(lines mkString "\n")
      assert(returnCode == ResultCode(77))
      assert(lines contains s"STDOUT $TestValue")
      assert(lines contains s"STDERR $TestValue")
    }
    finally process.close()
    Await.result(process.closed, 10.seconds)
    logger.info(s"$stopwatch for Java process")
  }
}

private object JavaProcessTest {
  private val TestValue = "TEST TEST"
  private val Arguments = Vector("a", "1 2")
  private val logger = Logger(getClass)

  def main(args: Array[String]): Unit = {
    if (args.toVector == Arguments) {
      out.println(s"STDOUT $TestValue")
      err.println(s"STDERR $TestValue")
      exit(77)
    } else
      exit(3)
  }
}