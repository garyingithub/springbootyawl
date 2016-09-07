package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.engine.Engine_Client;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedServer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 16-9-6.
 */
public abstract class MonitorRunnable implements Runnable{

    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    private String targetName;

    public MonitorRunnable(String targetName){
        this.targetName=targetName;

    }

    private Map<String,Integer> caseCounting=new ConcurrentHashMap<>();

    protected static ExecutorService executorService=Executors.newFixedThreadPool(2);
    private Engine_Client client=new Engine_Client();


    public synchronized void addRecord(String caseId,Integer curValue){
        Integer oldValue=caseCounting.get(caseId);
        caseCounting.put(caseId,getNewRecord(oldValue,curValue));
    }
    public abstract Integer getNewRecord(Integer oldValue,Integer curValue);



    private  String prefix;
    private synchronized void sendData()  {

        if(prefix==null&&InterfaceB_EngineBasedServer.engineId!=null){
            prefix=targetName+",host="+ InterfaceB_EngineBasedServer.engineId+",caseId=";
        }
        if(prefix!=null) {
            for (Map.Entry<String, Integer> e : caseCounting.entrySet()) {
                StringBuilder builder = new StringBuilder();
                builder.append(prefix);
                builder.append(e.getKey());
                builder.append(",tenantId=");
                builder.append(CaseTenantMap.getTenantId(e.getKey()));
                builder.append(" value=");
                builder.append(e.getValue());


                MonitorRunnable.executorService.execute(()->{
                    try {
                        long start=System.currentTimeMillis();

                        client.send(InterfaceB_EngineBasedServer.influxAddress, builder.toString());

                        logger.info(String.valueOf(System.currentTimeMillis()-start));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });


            }
            caseCounting.clear();

        }
    }


    @Override
    public void run() {
        while (true){
            sendData();
            try {

                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
