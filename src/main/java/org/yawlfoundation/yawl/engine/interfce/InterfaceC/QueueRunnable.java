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

    public QueueRunnable(int threadNumber,boolean isToLog,int waitTime){
        this.tenantQueueMap=new ConcurrentHashMap<>();
        this.service=Executors.newFixedThreadPool(threadNumber);
        this.isToLog=isToLog;
        this.waitTime=waitTime;
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


    private int waitTime=600;
    private boolean isToLog=false;

    @Override
    public void run() {

        for(;;){
            synchronized (this){

                for(String tenantId:tenantQueueMap.keySet()){
                    int capacity=TenantPriortyManagement.getPriority(tenantId);


                    BlockingQueue<TaskRunnable> queue=tenantQueueMap.get(tenantId);

                    int count=0;

                    if(queue.size()>0&&isToLog){
                        logger.info(String.format("%s %d request",tenantId,queue.size()));
                    }
                    while (queue.size()>0){
                        TaskRunnable handler= (TaskRunnable) tenantQueueMap.get(tenantId).poll();

                        service.submit(handler);
                        count++;
                    }

                    if(count>0&&isToLog){
                        logger.info(String.format("tenant %s has sent %d request",tenantId,count));
                    }

                    try {
                        if(tenantId.equals("2"))
                            this.wait(waitTime/2);
                        else
                            this.wait(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }

        }


    }
}
