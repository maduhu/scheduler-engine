package com.sos.scheduler.engine.playground.zschimmer.plugin.watchdog

import com.sos.scheduler.engine.common.time.Time
import com.sos.scheduler.engine.data.log.SchedulerLogLevel
import com.sos.scheduler.engine.data.order.OrderFinishedEvent
import com.sos.scheduler.engine.eventbus.HotEventHandler
import com.sos.scheduler.engine.kernel.log.PrefixLog
import com.sos.scheduler.engine.test.SchedulerTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit._

class WatchdogPluginIT extends SchedulerTest {
    private val schedulerTimeout = Time.of(15)
    private val sleepTime = Time.of(11)

    @Test def test() {
        controller.activateScheduler()
        Thread.sleep(schedulerTimeout.getMillis)
        assertThat(instance(classOf[PrefixLog]).lastByLevel(SchedulerLogLevel.warning), Matchers.startsWith("SCHEDULER-721"))
        controller.terminateScheduler()
    }

    @HotEventHandler def handleEvent(e: OrderFinishedEvent) {
        Thread.sleep(sleepTime.getMillis)   // Wir  blockieren den Scheduler
    }
}