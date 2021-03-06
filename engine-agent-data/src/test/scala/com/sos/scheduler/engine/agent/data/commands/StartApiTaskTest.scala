package com.sos.scheduler.engine.agent.data.commands

import com.sos.scheduler.engine.data.job.{JobPath, TaskId}
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner
import spray.json._

/**
 * @author Joacim Zschimmer
 */
@RunWith(classOf[JUnitRunner])
final class StartApiTaskTest extends FreeSpec {

  "JSON minimum" in {
    val obj = StartApiTask(
      meta = None,
      logon = None,
      javaOptions = "JAVA-OPTIONS",
      javaClasspath = "JAVA-CLASSPATH")
    val json = """{
      "$TYPE": "StartApiTask",
      "javaOptions": "JAVA-OPTIONS",
      "javaClasspath": "JAVA-CLASSPATH"
    }""".parseJson
    assert((obj: Command).toJson == json)   // Command serializer includes $TYPE
    assert(obj == json.convertTo[Command])
  }

  "JSON maximum" in {
    val obj = StartApiTask(
      meta = Some(StartTask.Meta(
        JobPath("/folder/test"),
        TaskId(123))),
      logon = Some(StartTask.KeyLogon(credentialsKey = "CREDENTIALS", withUserProfile = true)),
      javaOptions = "JAVA-OPTIONS",
      javaClasspath = "JAVA-CLASSPATH")
    val json = """{
      "$TYPE": "StartApiTask",
      "meta": {
        "job": "/folder/test",
        "taskId": "123"
      },
      "logon": {
        "credentialsKey": "CREDENTIALS",
        "withUserProfile": true
      },
      "javaOptions": "JAVA-OPTIONS",
      "javaClasspath": "JAVA-CLASSPATH"
    }""".parseJson
    assert((obj: Command).toJson == json)   // Command serializer includes $TYPE
    assert(obj == json.convertTo[Command])
  }
}
