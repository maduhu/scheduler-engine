// *** Generated by com.sos.scheduler.engine.cplusplus.generator ***

#include "_precompiled.h"

#include "com__sos__scheduler__engine__kernel__cppproxy__Prefix_logC.h"
#include "java__lang__Object.h"
#include "java__lang__String.h"

namespace javaproxy { namespace com { namespace sos { namespace scheduler { namespace engine { namespace kernel { namespace cppproxy { 

struct Prefix_logC__class : ::zschimmer::javabridge::Class
{
    Prefix_logC__class(const string& class_name);
   ~Prefix_logC__class();


    static const ::zschimmer::javabridge::class_factory< Prefix_logC__class > class_factory;
};

const ::zschimmer::javabridge::class_factory< Prefix_logC__class > Prefix_logC__class::class_factory ("com.sos.scheduler.engine.kernel.cppproxy.Prefix_logC");

Prefix_logC__class::Prefix_logC__class(const string& class_name) :
    ::zschimmer::javabridge::Class(class_name)
{}

Prefix_logC__class::~Prefix_logC__class() {}




Prefix_logC::Prefix_logC(jobject jo) { if (jo) assign_(jo); }

Prefix_logC::Prefix_logC(const Prefix_logC& o) { assign_(o.get_jobject()); }

#ifdef Z_HAS_MOVE_CONSTRUCTOR
    Prefix_logC::Prefix_logC(Prefix_logC&& o) { set_jobject(o.get_jobject());  o.set_jobject(NULL); }
#endif

Prefix_logC::~Prefix_logC() { assign_(NULL); }





::zschimmer::javabridge::Class* Prefix_logC::java_object_class_() const { return _class.get(); }

::zschimmer::javabridge::Class* Prefix_logC::java_class_() { return Prefix_logC__class::class_factory.clas(); }


void Prefix_logC::Lazy_class::initialize() const {
    _value = Prefix_logC__class::class_factory.clas();
}


}}}}}}}
