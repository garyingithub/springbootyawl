package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by root on 16-8-26.
 */
public class Task implements Callable,Comparable {

    private Logger _log= LoggerFactory.getLogger(this.getClass());

    public Task(HttpServletRequest request,EngineGatewayImpl _engine){
        this.request=request;
        this._engine=_engine;
    }

    @Override
    public Object call() throws Exception {
        return processPostQuery(request);
    }

    private HttpServletRequest request;
    private EngineGatewayImpl _engine;
    private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String workItemID = request.getParameter("workItemID");
        String specIdentifier = request.getParameter("specidentifier");
        String specVersion = request.getParameter("specversion");
        String specURI = request.getParameter("specuri");
        String taskID = request.getParameter("taskID");

        try {

            if (action != null) {
                if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnection(sessionHandle));
                }
                else if (action.equals("connect")) {
                    String userID = request.getParameter("userid");
                    String password = request.getParameter("password");
                    int interval = request.getSession().getMaxInactiveInterval();
                    msg.append(_engine.connect(userID, password, interval));
                }
                else if ("disconnect".equals(action)) {
                    msg.append(_engine.disconnect(sessionHandle));
                }
                else if (action.equals("checkout")) {
                    msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("checkin")) {
                    String data = request.getParameter("data");
                    String logPredicate = request.getParameter("logPredicate");
                    msg.append(_engine.completeWorkItem(workItemID, data, logPredicate, false,
                            sessionHandle));
                }
                else if (action.equals("rejectAnnouncedEnabledTask")) {
                    msg.append(_engine.rejectAnnouncedEnabledTask(workItemID, sessionHandle));
                }
                else if (action.equals("launchCase")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    URI completionObserver = getCompletionObserver(request);
                    String caseParams = request.getParameter("caseParams");
                    String logDataStr = request.getParameter("logData");
                    String mSecStr = request.getParameter("mSec");
                    String startStr = request.getParameter("start");
                    String waitStr = request.getParameter("wait");
                    String caseID=request.getParameter("caseID");
                    String tenantID=request.getParameter("tenantID");
                    if (mSecStr != null) {
                        msg.append(_engine.launchCase(specID, caseParams,
                                completionObserver, caseID,tenantID,logDataStr,
                                StringUtil.strToLong(mSecStr, 0), sessionHandle));
                    }
                    else if (startStr != null) {
                        long time = StringUtil.strToLong(startStr, 0);
                        Date date = time > 0 ? new Date(time) : new Date();
                        msg.append(_engine.launchCase(specID, caseParams,
                                completionObserver,caseID,tenantID,logDataStr, date, sessionHandle));
                    }
                    else if (waitStr != null) {
                        msg.append(_engine.launchCase(specID, caseParams,
                                completionObserver, caseID,tenantID,logDataStr,
                                StringUtil.strToDuration(waitStr), sessionHandle));
                    }
                    else msg.append(_engine.launchCase(specID, caseParams,
                                completionObserver, caseID,tenantID,logDataStr, sessionHandle));
                }
                else if (action.equals("cancelCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.cancelCase(caseID, sessionHandle));
                }
                else if (action.equals("getWorkItem")) {
                    msg.append(_engine.getWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("startOne")) {
                    String userID = request.getParameter("user");
                    msg.append(_engine.startWorkItem(userID, sessionHandle));
                }
                else if (action.equals("getLiveItems")) {
                    msg.append(_engine.describeAllWorkItems(sessionHandle));
                }
                else if (action.equals("getAllRunningCases")) {
                    msg.append(_engine.getAllRunningCases(sessionHandle));
                }
                else if (action.equals("getWorkItemsWithIdentifier")) {
                    String idType = request.getParameter("idType");
                    String id = request.getParameter("id");
                    msg.append(_engine.getWorkItemsWithIdentifier(idType, id, sessionHandle));
                }
                else if (action.equals("getWorkItemsForService")) {
                    String serviceURI = request.getParameter("serviceuri");
                    msg.append(_engine.getWorkItemsForService(serviceURI, sessionHandle));
                }
                else if (action.equals("taskInformation")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getTaskInformation(specID, taskID, sessionHandle));
                }
                else if (action.equals("getMITaskAttributes")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getMITaskAttributes(specID, taskID, sessionHandle));
                }
                else if (action.equals("getResourcingSpecs")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getResourcingSpecs(specID, taskID, sessionHandle));
                }
                else if (action.equals("checkIsAdmin")) {
                    msg.append(_engine.checkConnectionForAdmin(sessionHandle));
                }
                else if (action.equals("checkAddInstanceEligible")) {
                    msg.append(_engine.checkElegibilityToAddInstances(
                            workItemID, sessionHandle));
                }
                else if (action.equals("getSpecificationPrototypesList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                }
                else if (action.equals("getSpecification")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getProcessDefinition(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationData")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getSpecificationData(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationDataSchema")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getSpecificationDataSchema(specID, sessionHandle));
                }
                else if (action.equals("getCasesForSpecification")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getCasesForSpecification(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationForCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getSpecificationForCase(caseID, sessionHandle));
                }
                else if (action.equals("getSpecificationIDForCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getSpecificationIDForCase(caseID, sessionHandle));
                }
                else if (action.equals("getCaseState")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseState(caseID, sessionHandle));
                }
                else if (action.equals("getCaseData")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseData(caseID, sessionHandle));
                }
                else if (action.equals("getChildren")) {
                    msg.append(_engine.getChildrenOfWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("getWorkItemExpiryTime")) {
                    msg.append(_engine.getWorkItemExpiryTime(workItemID, sessionHandle));
                }
                else if (action.equals("getCaseInstanceSummary")) {
                    msg.append(_engine.getCaseInstanceSummary(sessionHandle));
                }
                else if (action.equals("getWorkItemInstanceSummary")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getWorkItemInstanceSummary(caseID, sessionHandle));
                }
                else if (action.equals("getParameterInstanceSummary")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getParameterInstanceSummary(caseID, workItemID, sessionHandle));
                }
                else if (action.equals("createInstance")) {
                    String paramValueForMICreation =
                            request.getParameter("paramValueForMICreation");
                    msg.append(_engine.createNewInstance(workItemID,
                            paramValueForMICreation, sessionHandle));
                }
                else if (action.equals("suspend")) {
                    msg.append(_engine.suspendWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("rollback")) {
                    msg.append(_engine.rollbackWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("unsuspend")) {
                    msg.append(_engine.unsuspendWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("skip")) {
                    msg.append(_engine.skipWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("getStartingDataSnapshot")) {
                    msg.append(_engine.getStartingDataSnapshot(workItemID, sessionHandle));
                }
            }  // action is null
            else if (request.getRequestURI().endsWith("ib")) {
                msg.append(_engine.getAvailableWorkItemIDs(sessionHandle));
            }
            else if (request.getRequestURI().contains("workItem")) {
                msg.append(_engine.getWorkItemOptions(workItemID,
                        request.getRequestURL().toString(), sessionHandle));
            }
            else _log.error("Interface B called with null action.");
        }
        catch (RemoteException e) {
            _log.error("Remote Exception in Interface B with action: " + action, e);
        }
        _log.debug("InterfaceB_EngineBasedServer::doPost() result = {}", msg);
        return msg.toString();
    }
    private URI getCompletionObserver(HttpServletRequest request) {
        String completionObserver = request.getParameter("completionObserverURI");
        if(completionObserver != null) {
            try {
                return new URI(completionObserver);
            } catch (URISyntaxException e) {
                _log.error("Failure to ", e);
            }
        }
        return null;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
