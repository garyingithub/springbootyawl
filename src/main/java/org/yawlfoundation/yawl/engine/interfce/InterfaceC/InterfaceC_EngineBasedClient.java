package org.yawlfoundation.yawl.engine.interfce.InterfaceC;


import edu.sysu.yawl.Property;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.Dispatcher;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gary on 16-8-11.
 */

public class InterfaceC_EngineBasedClient {


    private static final ExecutorService countingExecutor=
            Executors.newFixedThreadPool(1);
    private Engine_Client client=new Engine_Client();

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private InterfaceC_EngineBasedClient.MonitorClient monitorClient;
    private String influxAddress="http://222.200.180.59:8086/write?db=yawlcloud";
    public InterfaceC_EngineBasedClient(){

        monitorClient=new InterfaceC_EngineBasedClient.MonitorClient();
        monitorClient.start();
    }
    private Map<String,String> caseTenant=new HashMap<>();


    public void addCounting(String caseId,String tenantId){
        monitorClient.addCounting(caseId,tenantId);
    }

    class MonitorClient extends Thread{

        private Map<String,Integer> caseCounting=new ConcurrentHashMap<>();


        private Object mapLock=new Object();

        public void addCounting(String caseId,String tenantId){

            caseCounting.putIfAbsent(caseId,0);
            caseCounting.put(caseId,caseCounting.get(caseId)+1);

            caseTenant.put(caseId,tenantId);
        }





        private final String prefix="caseCounting,host="+InterfaceB_EngineBasedServer.engineId+",caseId=";
        private void sendData(){
            synchronized (mapLock) {
                for (Map.Entry<String, Integer> e : caseCounting.entrySet()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(prefix);
                    builder.append(e.getKey());
                    builder.append(",tenantId=");
                    builder.append(caseTenant.get(e.getKey()));
                    builder.append(" value=");
                    builder.append(e.getValue());
                    countingExecutor.execute(() -> {
                        try {
                            client.send(influxAddress, builder.toString());
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
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }


        }
    }








}
