// *** Generated by com.sos.scheduler.engine.cplusplus.generator ***

#include "_precompiled.h"

#include "com__sos__scheduler__engine__kernel__cppproxy__Job_nodeC.h"
#include "com__sos__scheduler__engine__kernel__cppproxy__JobC.h"
#include "java__lang__Object.h"
#include "java__lang__String.h"

namespace javaproxy { namespace com { namespace sos { namespace scheduler { namespace engine { namespace kernel { namespace cppproxy { 

struct Job_nodeC__class : ::zschimmer::javabridge::Class
{
    Job_nodeC__class(const string& class_name);
   ~Job_nodeC__class();


    static const ::zschimmer::javabridge::class_factory< Job_nodeC__class > class_factory;
};

const ::zschimmer::javabridge::class_factory< Job_nodeC__class > Job_nodeC__class::class_factory ("com.sos.scheduler.engine.kernel.cppproxy.Job_nodeC");

Job_nodeC__class::Job_nodeC__class(const string& class_name) :
    ::zschimmer::javabridge::Class(class_name)
{}

Job_nodeC__class::~Job_nodeC__class() {}




Job_nodeC::Job_nodeC(jobject jo) { if (jo) assign_(jo); }

Job_nodeC::Job_nodeC(const Job_nodeC& o) { assign_(o.get_jobject()); }

#ifdef Z_HAS_MOVE_CONSTRUCTOR
    Job_nodeC::Job_nodeC(Job_nodeC&& o) { set_jobject(o.get_jobject());  o.set_jobject(NULL); }
#endif

Job_nodeC::~Job_nodeC() { assign_(NULL); }





::zschimmer::javabridge::Class* Job_nodeC::java_object_class_() const { return _class.get(); }

::zschimmer::javabridge::Class* Job_nodeC::java_class_() { return Job_nodeC__class::class_factory.clas(); }


void Job_nodeC::Lazy_class::initialize() const {
    _value = Job_nodeC__class::class_factory.clas();
}


}}}}}}}
