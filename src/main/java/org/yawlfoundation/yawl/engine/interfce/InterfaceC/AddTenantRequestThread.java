package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.Dispatcher;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by root on 16-8-30.
 */
@Component
public class AddTenantRequestThread extends Thread {

    private Logger _logger= LoggerFactory.getLogger(this.getClass());

    private  Map<String,Queue<InterfaceB_EngineBasedClient.Handler>> queues=new HashMap<>();

    public AtomicInteger executeCount=new AtomicInteger();


    @Autowired
    private ExecuteThread executeThread=ExecuteThread.getExecuteThread();

    @Autowired
    private TenantPriortyManagement tenantPriortyManagement;

    public void addTenantHandler(InterfaceB_EngineBasedClient.Handler handler){
        String tenantId=handler.getTenantId().toString();
        synchronized (this){
            this.queues.putIfAbsent(tenantId,new ArrayDeque<>());
        }

   //     this._logger.info(String.format("tenant %s is sending %s",tenantId,handler.getEvent()));
        this.queues.get(tenantId).add(handler);
    }



    @Override
    public void run() {
        Map<String,Integer> tenantQuantumMap=new HashMap<>();

        for(;;){

            //_logger.info(String.valueOf(this.queues.size()));
            boolean ok=true;
            for(String tenantId:this.queues.keySet()){
                if(this.queues.get(tenantId).size()==0)
                    ok=false;
            }
            if(!ok) continue;

            for(String tenantId: this.queues.keySet()){

                tenantQuantumMap.putIfAbsent(tenantId,0);
                int count=0;

                int quota=tenantQuantumMap.get(tenantId)+ TenantPriortyManagement.getPriority(tenantId);

                Queue tenantQueue=this.queues.get(tenantId);

                while (!tenantQueue.isEmpty()&&quota>0){
                    InterfaceB_EngineBasedClient.Handler task= (InterfaceB_EngineBasedClient.Handler) tenantQueue.poll();

                    if(task!=null)
                        executeThread.addExecuteHandler(task);
                    quota--;
                    count++;


                }
                if(count>0)
                      _logger.info(String.format("tenant %s has send %d requests",tenantId,count));
                if(quota>=0)
                    tenantQuantumMap.put(tenantId,quota);
                if(tenantQueue.isEmpty()){
                    tenantQuantumMap.put(tenantId,0);
                }

            }

        }
    }
}
