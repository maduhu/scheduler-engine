package com.sos.scheduler.engine.kernel.order.jobchain;

import com.google.common.collect.ImmutableList;
import com.sos.scheduler.engine.cplusplus.runtime.Sister;
import com.sos.scheduler.engine.cplusplus.runtime.SisterType;
import com.sos.scheduler.engine.data.folder.AbsolutePath;
import com.sos.scheduler.engine.data.folder.FileBasedType;
import com.sos.scheduler.engine.data.order.OrderId;
import com.sos.scheduler.engine.kernel.cppproxy.Job_chainC;
import com.sos.scheduler.engine.kernel.folder.FileBased;
import com.sos.scheduler.engine.kernel.order.Order;

public final class JobChain extends FileBased implements UnmodifiableJobchain {
    private final Job_chainC cppProxy;

    private JobChain(Job_chainC cppProxy) {
        this.cppProxy = cppProxy;
    }
    
    @Override public void onCppProxyInvalidated() {}

    @Override public String getName() {
        return cppProxy.name();
    }

    @Override public FileBasedType getFileBasedType() {
        return FileBasedType.jobChain;
    }

    @Override public AbsolutePath getPath() {
        return new AbsolutePath(cppProxy.path());
    }

    /** Markiert, dass das {@link FileBased} beim nächsten Verzeichnisabgleich neu geladen werden soll. */
    public void setForceFileReread() {
        cppProxy.set_force_file_reread();
    }

	public ImmutableList<Node> getNodes() { 
        return ImmutableList.copyOf(cppProxy.java_nodes());
    }

	public Order order(OrderId id) {
        return cppProxy.order(id.asString()).getSister();
    }

    @Override public String toString() {
        return JobChain.class.getSimpleName() + " " + getName();
    }

    public static class Type implements SisterType<JobChain, Job_chainC> {
        @Override public final JobChain sister(Job_chainC proxy, Sister context) {
            return new JobChain(proxy);
        }
    }
}