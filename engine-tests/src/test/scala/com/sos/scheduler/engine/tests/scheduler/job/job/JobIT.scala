package com.sos.scheduler.engine.tests.scheduler.job.job

import com.sos.scheduler.engine.data.job.{JobOverview, JobPath}
import com.sos.scheduler.engine.kernel.job.JobSubsystemClient
import com.sos.scheduler.engine.test.scalatest.ScalaSchedulerTest
import com.sos.scheduler.engine.tests.scheduler.job.job.JobIT._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JobIT extends FunSuite with ScalaSchedulerTest {

  private lazy val job = instance[JobSubsystemClient].job(jobPath)
  private lazy val jobOverview = instance[JobSubsystemClient].jobView[JobOverview](jobPath)

  test("job.name") {
    assert(jobOverview.path.name === "a")
  }

  test("job.path") {
    assert(jobOverview.path.string === "/a")
  }

  test("job.fileBasedIsReread") {
    assert(job.fileBasedIsReread === false)
  }

  test("jobSubsystem.visibleNames") {
    instance[JobSubsystemClient].visiblePaths.toSet shouldEqual Set(JobPath("/a"), JobPath("/b"))
  }

  test("jobSubsystem.names") {
    instance[JobSubsystemClient].paths.toSet shouldEqual Set(JobPath("/scheduler_file_order_sink"), JobPath("/scheduler_service_forwarder"), JobPath("/a"), JobPath("/b"))
  }
}


private object JobIT {
  private val jobPath = JobPath("/a")
}
