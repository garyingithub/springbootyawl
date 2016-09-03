
package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient.Handler;
public class ExecuteThread extends Thread{

    private Map<String,BlockingQueue> tenantQueueMap=new ConcurrentHashMap<>();

    public void addHandler(Handler handler){
        String tenantId=handler.getTenantId().toString();
        synchronized (this){
            if(!tenantQueueMap.containsKey(tenantId)){
                int capacity=TenantPriortyManagement.getPriority(tenantId);
                tenantQueueMap.putIfAbsent(tenantId,new ArrayBlockingQueue(capacity));
            }
        }
        try {
            tenantQueueMap.get(tenantId).put(handler);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

  //  private Map<String,Integer> tenantQuotaMap=new HashMap<>();
    private ExecutorService service= Executors.newCachedThreadPool();

    @Override
    public void run() {

        for(;;){
            synchronized (this){
                for(String tenantId:tenantQueueMap.keySet()){
                    int capacity=TenantPriortyManagement.getPriority(tenantId);
                //    tenantQuotaMap.putIfAbsent(tenantId,0);
                    int quota=capacity;

                    while (quota>0){
                        Handler handler= (Handler) tenantQueueMap.get(tenantId).poll();
                        if(handler==null){
                            break;
                        }else {
                            service.submit(handler);
                        }
                    }

                }
            }
        }

    }
}