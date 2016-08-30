package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 16-8-30.
 */
@Component
public class TenantPriortyManagement {

    private static Map<String,Integer> tenantPriorityMap=new ConcurrentHashMap<>();

    public static void addTenant(String tenantid,Integer priority){
        TenantPriortyManagement.tenantPriorityMap.put(tenantid,priority);
    }

    public static Integer getPriority(String tenantId){
        return TenantPriortyManagement.tenantPriorityMap.get(tenantId);
    }
}
