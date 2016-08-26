package org.yawlfoundation.yawl.engine.interfce.InterfaceC;


import org.jdom2.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
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

public class InterfaceC_EngineBasedClient extends Engine_Client implements ObserverGateway {

    private static final ExecutorService countingExecutor=
            Executors.newFixedThreadPool(1);

    String engineId;
    private InterfaceC_EngineBasedClient.MonitorClient monitorClient;
    private String influxAddress="http://222.200.180.59:8086/write?db=yawlcloud";
    public InterfaceC_EngineBasedClient(){

    }
    private Map<String,String> caseTenant=new HashMap<>();


    class MonitorClient extends Thread{

        private Map<String,Integer> caseCounting=new ConcurrentHashMap<>();


        private Object mapLock=new Object();

        public void addCounting(String caseId){

            caseCounting.putIfAbsent(caseId,0);
            caseCounting.put(caseId,caseCounting.get(caseId)+1);

        }




        private final String prefix="caseCounting,host="+engineId+",caseId=";
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
                            send(influxAddress, builder.toString());
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
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }


        }
    }





    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public void announceFiredWorkItem(YAnnouncement announcement) {
        YWorkItem workItem=announcement.getItem();

        String caseId=workItem.getCaseID().getRootAncestor().get_idString();
        caseTenant.putIfAbsent(caseId,workItem.getCaseID().getTenantId());
        monitorClient.addCounting(caseId);
    }

    @Override
    public void announceCancelledWorkItem(YAnnouncement announcement) {

    }

    @Override
    public void announceTimerExpiry(YAnnouncement announcement) {

    }

    @Override
    public void announceCaseCompletion(YAWLServiceReference yawlService, YIdentifier caseID, Document caseData) {

    }

    @Override
    public void announceCaseStarted(Set<YAWLServiceReference> services, YSpecificationID specID, YIdentifier caseID, String launchingService, boolean delayed) {

    }

    @Override
    public void announceCaseCompletion(Set<YAWLServiceReference> services, YIdentifier caseID, Document caseData) {

    }

    @Override
    public void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceWorkItemStatusChange(Set<YAWLServiceReference> services, YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus) {

        String caseId=workItem.getCaseID().getRootAncestor().get_idString();
        caseTenant.putIfAbsent(caseId,workItem.getCaseID().getTenantId());
        monitorClient.addCounting(caseId);
    }

    @Override
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) {

        engineId= InterfaceB_EngineBasedServer.engineId;

        if(InterfaceB_EngineBasedServer.influxAddress!=null){
            this.influxAddress= InterfaceB_EngineBasedServer.influxAddress;
        }
        monitorClient=new InterfaceC_EngineBasedClient.MonitorClient();
        monitorClient.start();

    }

    @Override
    public void announceCaseCancellation(Set<YAWLServiceReference> services, YIdentifier id) {

    }

    @Override
    public void announceDeadlock(Set<YAWLServiceReference> services, YIdentifier id, Set<YTask> tasks) {

    }

    @Override
    public void shutdown() {

    }

    public static void main(String[] args){
        Interface_Client client=new Interface_Client();
        Map<String,String> params=new HashMap<>();
        params.put("action","test");
        for(int i=0;i<3;i++){
            try {
                client.originalExecutePost("http://localhost:8080/yawl/ib",params);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
