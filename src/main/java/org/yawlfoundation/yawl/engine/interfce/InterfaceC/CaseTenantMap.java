package org.yawlfoundation.yawl.engine.interfce.InterfaceC;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 16-9-6.
 */
public class CaseTenantMap {

    private static Map<String,String> caseTenantMap=new HashMap<>();

    public static void addCase(String caseId,String tenantId){
        CaseTenantMap.caseTenantMap.put(caseId,tenantId);
    }

    public static String getTenantId(String caseId){
        return CaseTenantMap.caseTenantMap.get(caseId);
    }

}
