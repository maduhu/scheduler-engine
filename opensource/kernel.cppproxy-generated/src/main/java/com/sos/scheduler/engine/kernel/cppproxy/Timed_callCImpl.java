// *** Generated by com.sos.scheduler.engine.cplusplus.generator ***

package com.sos.scheduler.engine.kernel.cppproxy;

final class Timed_callCImpl
   extends com.sos.scheduler.engine.cplusplus.runtime.CppProxyImpl<com.sos.scheduler.engine.kernel.async.CppCall>
   implements com.sos.scheduler.engine.kernel.cppproxy.Timed_callC
{
    private Timed_callCImpl(com.sos.scheduler.engine.cplusplus.runtime.Sister context) { // Nur für JNI zugänglich
        setSister(sisterType.sister(this, context));
    }

    @Override public long at_millis() {
        com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.lock();
        try {
            return at_millis__native(cppReference());
        }
        catch (Exception x) { throw com.sos.scheduler.engine.cplusplus.runtime.CppProxies.propagateCppException(x, this); }
        finally {
            com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.unlock();
        }
    }

    private static native long at_millis__native(long cppReference);


    @Override public void call() {
        com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.lock();
        try {
            call__native(cppReference());
        }
        catch (Exception x) { throw com.sos.scheduler.engine.cplusplus.runtime.CppProxies.propagateCppException(x, this); }
        finally {
            com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.unlock();
        }
    }

    private static native void call__native(long cppReference);


    @Override public java.lang.String obj_name() {
        com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.lock();
        try {
            java.lang.String result = obj_name__native(cppReference());
            checkIsNotReleased(java.lang.String.class, result);
            return result;
        }
        catch (Exception x) { throw com.sos.scheduler.engine.cplusplus.runtime.CppProxies.propagateCppException(x, this); }
        finally {
            com.sos.scheduler.engine.cplusplus.runtime.CppProxy.threadLock.unlock();
        }
    }

    private static native java.lang.String obj_name__native(long cppReference);


}