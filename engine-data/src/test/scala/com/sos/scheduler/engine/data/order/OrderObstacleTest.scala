package com.sos.scheduler.engine.data.order

import com.sos.scheduler.engine.data.order.OrderObstacle._
import java.time.Instant
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner
import spray.json._

/**
  * @author Joacim Zschimmer
  */
@RunWith(classOf[JUnitRunner])
final class OrderObstacleTest extends FreeSpec {

  "JSON" - {
    addTest(Suspended,
      """{
        "TYPE": "Suspended"
      }""")
    addTest(Blacklisted,
      """{
        "TYPE": "Blacklisted"
      }""")
    addTest(Setback(Instant.parse("2016-08-01T11:22:33.444Z")),
      """{
        "TYPE": "Setback",
        "until": "2016-08-01T11:22:33.444Z"
      }""")
  }

  private def addTest(obstacle: OrderObstacle, json: String): Unit = {
    s"$obstacle" in {
      val jsValue = json.parseJson
      assert(obstacle.toJson == jsValue)
      assert(jsValue.convertTo[OrderObstacle] == obstacle)
    }
  }
}
