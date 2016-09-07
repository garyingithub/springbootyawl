package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by root on 16-9-6.
 */
public class QueueRunnable implements Runnable {

    private Map<String,BlockingQueue> tenantQueueMap;
    Logger logger= LoggerFactory.getLogger(this.getClass());

    public QueueRunnable(int threadNumber){
        this.tenantQueueMap=new ConcurrentHashMap<>();
        this.service=Executors.newFixedThreadPool(threadNumber);
    }

    public void addHandler(TaskRunnable handler){
        String tenantId=handler.getTenantId();
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


    private ExecutorService service;



    @Override
    public void run() {

        Map<String,Integer> quotaMap=new ConcurrentHashMap<>();
        for(;;){
            synchronized (this){

                for(String tenantId:tenantQueueMap.keySet()){
                    int capacity=TenantPriortyManagement.getPriority(tenantId);

                    quotaMap.putIfAbsent(tenantId,0);
                    int quota=capacity+quotaMap.get(tenantId);
                    quotaMap.put(tenantId,quota);

                    BlockingQueue<TaskRunnable> queue=tenantQueueMap.get(tenantId);

                    int count=0;

                    if(queue.size()>0){
                        logger.info(String.format("%s  %d %d request",tenantId,quota,queue.peek().getConsume()));

                    }
                    while (queue.size()>0 &&
                            quota>=queue.peek().getConsume()){
                        quota-=queue.peek().getConsume();
                        quotaMap.put(tenantId,quota);

                        TaskRunnable handler= (TaskRunnable) tenantQueueMap.get(tenantId).poll();

                        service.submit(handler);
                        count++;
                    }
                    if(queue.size()==0){
                        quotaMap.put(tenantId,0);
                    }
                    if(count>0){
                        logger.info(String.format("tenant %s has sent %d request",tenantId,count));
                    }

                    try {
                        this.wait(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }

        }


    }
}
