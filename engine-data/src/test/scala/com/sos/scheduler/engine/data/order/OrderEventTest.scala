package com.sos.scheduler.engine.data.order

import com.sos.scheduler.engine.data.event.{AnyKeyedEvent, KeyedEvent}
import com.sos.scheduler.engine.data.events.SchedulerAnyKeyedEventJsonFormat
import com.sos.scheduler.engine.data.job.TaskId
import com.sos.scheduler.engine.data.jobchain.{JobChainPath, NodeId}
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner
import spray.json._

/**
  * @author Joacim Zschimmer
  */
@RunWith(classOf[JUnitRunner])
final class OrderEventTest extends FreeSpec {

  private val orderKey = JobChainPath("/JOB-CHAIN") orderKey "ORDER-ID"

  "OrderAdded" in {
    check(KeyedEvent(OrderAdded(NodeId("START")))(orderKey),
      """{
        "TYPE": "OrderAdded",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeId": "START"
      }""")
  }

  "OrderRemoved" in {
    check(KeyedEvent(OrderRemoved)(orderKey),
      """{
        "TYPE": "OrderRemoved",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
  }

  "OrderFinished" in {
    check(KeyedEvent(OrderFinished(NodeId("END")))(orderKey),
      """{
        "TYPE": "OrderFinished",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeId": "END"
      }""")
  }

  "OrderNestedFinished" in {
    check(KeyedEvent(OrderNestedFinished)(orderKey),
      """{
        "TYPE": "OrderNestedFinished",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
    }

  "OrderNestedStarted" in {
    check(KeyedEvent(OrderNestedStarted)(orderKey),
      """{
        "TYPE": "OrderNestedStarted",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
  }

  "OrderResumed" in {
    check(KeyedEvent(OrderResumed)(orderKey),
      """{
        "TYPE": "OrderResumed",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
  }

  "OrderSetBack" in {
    check(KeyedEvent(OrderSetBack(NodeId("100")))(orderKey),
      """{
        "TYPE": "OrderSetBack",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeId": "100"
      }""")
  }

  "OrderNodeChanged" in {
    check(KeyedEvent(OrderNodeChanged(NodeId("100"), fromNodeId = NodeId("50")))(orderKey),
      """{
        "TYPE": "OrderNodeChanged",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeId": "100",
        "fromNodeId": "50"
      }""")
  }

  "OrderStepEnded" in {
    check(KeyedEvent(OrderStepEnded(OrderNodeTransition.Success))(orderKey),
      """{
        "TYPE": "OrderStepEnded",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeTransition": {
          "TYPE": "Success"
        }
      }""")
  }

  "OrderStepStarted" in {
    check(KeyedEvent(OrderStepStarted(NodeId("100"), TaskId(123)))(orderKey),
      """{
        "TYPE": "OrderStepStarted",
        "key": "/JOB-CHAIN,ORDER-ID",
        "nodeId": "100",
        "taskId": "123"
      }""")
  }

  "OrderSuspended" in {
    check(KeyedEvent(OrderSuspended)(orderKey),
      """{
        "TYPE": "OrderSuspended",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
    }

  "OrderStarted" in {
    check(KeyedEvent(OrderStarted)(orderKey),
      """{
        "TYPE": "OrderStarted",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
    }

  "OrderWaitingInTask" in {
    check(KeyedEvent(OrderWaitingInTask)(orderKey),
      """{
        "TYPE": "OrderWaitingInTask",
        "key": "/JOB-CHAIN,ORDER-ID"
      }""")
    }

  private def check(event: AnyKeyedEvent, json: String): Unit = {
    val jsValue = json.parseJson
    assert(event.toJson == jsValue)
    assert(event == jsValue.convertTo[AnyKeyedEvent] )
  }
}
