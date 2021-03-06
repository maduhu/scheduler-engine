package com.sos.scheduler.engine.taskserver.modules.monitors

import com.sos.scheduler.engine.data.log.SchedulerLogLevel
import com.sos.scheduler.engine.taskserver.moduleapi.ModuleArguments
import com.sos.scheduler.engine.taskserver.modules.javamodule.ApiModule
import com.sos.scheduler.engine.taskserver.spoolerapi.TypedNamedIDispatches

/**
  * @author Joacim Zschimmer
  */
object Monitors {
  def newMonitorInstance(args: ModuleArguments, namedIDispatches: TypedNamedIDispatches, stderrLogLevel: SchedulerLogLevel): sos.spooler.IMonitor_impl =
    args.newModule() match {
      case module: ApiModule ⇒ module.newMonitorInstance(namedIDispatches, stderrLogLevel)
      case module ⇒ throw new IllegalArgumentException(s"Unsupported module class '${module.getClass.getSimpleName}' for a monitor")
    }
}
