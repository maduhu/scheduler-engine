package com.sos.scheduler.engine.tests.scheduler.job.job

import com.sos.scheduler.engine.data.folder.JobPath
import com.sos.scheduler.engine.kernel.job.JobSubsystem
import com.sos.scheduler.engine.test.scala.ScalaSchedulerTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class JobIT extends ScalaSchedulerTest {

  import JobIT._

  private lazy val job = instance[JobSubsystem].job(jobPath)

  test("job.name") {
    assert(job.getName === "a")
  }

  test("job.path") {
    assert(job.getPath.asString === "/a")
  }

  test("job.isFileBasedReread") {
    assert(job.isFileBasedReread === false)
  }

  test("jobSubsystem.visibleNames") {
    val list = instance[JobSubsystem].getVisibleNames.toList
    list.toSet should equal (Set("a", "b"))
  }

  test("jobSubsystem.names") {
    val list = instance[JobSubsystem].getNames.toList
    list.toSet should equal (Set("scheduler_file_order_sink", "scheduler_service_forwarder", "a", "b"))
  }
}

object JobIT {
  private val jobPath = JobPath.of("/a")
}