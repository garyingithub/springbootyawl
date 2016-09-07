package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

/**
 * Created by root on 16-9-6.
 */
public interface TaskRunnable extends Runnable  {

    public String getCaseId();
    public String getTenantId();

    public Integer getConsume();

}
