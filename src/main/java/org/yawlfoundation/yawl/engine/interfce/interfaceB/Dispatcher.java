package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.engine.interfce.InterfaceC.ExecuteThread;

import java.nio.file.attribute.DosFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by root on 16-8-26.
 */
public class Dispatcher  {

  //  public static ExecutorService dispatchService=Executors.newFixedThreadPool(2);
    public static ArrayBlockingQueue<Task> taskQueue=new ArrayBlockingQueue<>(5);
    public static ExecutorService service= Executors.newFixedThreadPool(5);

    private static Map<String,Integer> casePriorityMap=new HashMap<>();

    public static void insertCase(String caseId,int priority){
        Dispatcher.casePriorityMap.put(caseId,priority);
    }

    public static int getCasePriority(String caseId){
        Integer result;
        do{
            result=Dispatcher.casePriorityMap.get(caseId);
        }while (result==null);

        return result;
    }

    public static void addTask(Task task){
        try {
            Dispatcher.taskQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
