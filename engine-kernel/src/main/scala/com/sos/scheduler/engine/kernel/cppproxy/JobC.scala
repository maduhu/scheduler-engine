package com.sos.scheduler.engine.kernel.cppproxy

import com.sos.scheduler.engine.cplusplus.runtime.CppProxyWithSister
import com.sos.scheduler.engine.cplusplus.runtime.annotation.{CppClass, CppExpression}
import com.sos.scheduler.engine.data.job.TaskPersistentState
import com.sos.scheduler.engine.kernel.job.{Job, Task}

@CppClass(clas = "sos::scheduler::Job", directory = "scheduler", include = "spooler.h")
trait JobC extends CppProxyWithSister[Job] with File_basedC[Job] {
  def default_process_class_path: String
  def is_in_period: Boolean
  def max_tasks: Int
  def running_tasks_count: Int
  def script_text: String
  def title: String
  def description: String
  def state_name: String
  def state_text: String
  def set_state_cmd(cmd: String): Unit
  def is_permanently_stopped: Boolean
  def next_start_time_millis: Long
  def next_possible_start_millis: Long
  def enqueue_taskPersistentState(o: TaskPersistentState): Unit
  def waiting_for_process: Boolean
  def unavailable_lock_path_strings: Array[String]

  @CppExpression("$->is_task_ready_for_order((::sos::scheduler::Process_class*)p0)")
  def is_task_ready_for_order(processClassPtr: Long): Boolean

  def java_tasks: java.util.ArrayList[Task]
}

object JobC {
  val sisterType = Job.Type
}
