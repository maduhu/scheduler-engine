package com.sos.scheduler.engine.data;

import com.google.common.collect.ImmutableList;
import com.sos.scheduler.engine.data.event.Event;
import com.sos.scheduler.engine.data.folder.FileBasedActivatedEvent;
import com.sos.scheduler.engine.data.folder.FileBasedRemovedEvent;
import com.sos.scheduler.engine.data.job.TaskEndedEvent;
import com.sos.scheduler.engine.data.job.TaskStartedEvent;
import com.sos.scheduler.engine.data.order.*;

public final class EventList {
    public static final ImmutableList<Class<? extends Event>> eventClassList = ImmutableList.<Class<? extends Event>>of(
            FileBasedActivatedEvent.class,
            FileBasedRemovedEvent.class,
            TaskStartedEvent.class,
            TaskEndedEvent.class,
            OrderTouchedEvent.class,
            OrderFinishedEvent.class,
            OrderSuspendedEvent.class,
            OrderResumedEvent.class,
            OrderStepStartedEvent.class,
            OrderStepEndedEvent.class,
            OrderStateChangedEvent.class);

    public static Class<?>[] eventClassArray() {
        return eventClassList.toArray(new Class<?>[0]);
    }

    private EventList() {}
}