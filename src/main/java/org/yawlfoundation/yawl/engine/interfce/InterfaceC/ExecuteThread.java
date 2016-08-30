package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.springframework.stereotype.Component;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by root on 16-8-30.
 */
@Component
public class ExecuteThread extends Thread {

    private ExecuteThread(){

    }

    private static ExecuteThread single=new ExecuteThread();

    public static ExecuteThread getExecuteThread(){
        return single;
    }

    private static final ExecutorService _executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ArrayBlockingQueue<InterfaceB_EngineBasedClient.Handler> executeQueue=new ArrayBlockingQueue<InterfaceB_EngineBasedClient.Handler>(5);
    private AtomicInteger tokenNumber=new AtomicInteger(Runtime.getRuntime().availableProcessors());


    public void addExecuteHandler(InterfaceB_EngineBasedClient.Handler handler){
        try {
            executeQueue.put(handler);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseToken(){
        tokenNumber.getAndIncrement();
    }
    @Override
    public void run() {

        for(;;)
        if (tokenNumber.get()>0){
            InterfaceB_EngineBasedClient.Handler handler;
            try {
                handler = executeQueue.take();
                _executor.submit(handler);
                tokenNumber.decrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
