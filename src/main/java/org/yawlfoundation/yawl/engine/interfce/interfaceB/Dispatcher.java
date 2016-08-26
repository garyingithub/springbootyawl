package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 16-8-26.
 */
public class Dispatcher  {

    public static ExecutorService service=new ThreadPoolExecutor(3, 3,
            0L, TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<Runnable>());;
}
