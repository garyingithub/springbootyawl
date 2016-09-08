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

public class EngineMonitor {

    private Engine_Client client=new Engine_Client();

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private MonitorRunnable dispatchRecord=new MonitorRunnable("DispatchRecord") {

        @Override
        public Integer getNewRecord(Integer oldValue, Integer curValue) {
            if(oldValue==null){
                oldValue=curValue;
            }
            return (oldValue+curValue)/2;
        }
    };

    private MonitorRunnable responseRecord=new MonitorRunnable("ResponseRecord") {
        @Override
        public Integer getNewRecord(Integer oldValue, Integer curValue) {
            if(oldValue==null){
                oldValue=curValue;
            }
            return (oldValue+curValue)/2;
        }
    };


    private EngineMonitor(){
        new Thread(dispatchRecord).start();
        new Thread(responseRecord).start();


    }

    private static EngineMonitor engineMonitor;

    public static EngineMonitor getMonitor(){
        if(engineMonitor==null){
            engineMonitor=new EngineMonitor();
        }
        return engineMonitor;
    }


    private Map<String,String> caseTenant=new HashMap<>();


    public void addCounting(String caseId,long value){
        dispatchRecord.addRecord(caseId, Math.toIntExact(value));
    }

    public void addResponseTime(String caseId,Integer responseTime){
        responseRecord.addRecord(caseId,responseTime);
    }










}
