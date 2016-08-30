/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import edu.sysu.yawl.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.yawlfoundation.yawl.elements.data.external.ExternalDBGatewayFactory;
import org.yawlfoundation.yawl.elements.predicate.PredicateEvaluatorFactory;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGateway;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;


/**
 * Receives & responds to POST messages from custom services
 *
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 *
 * @author Michael Adams (refactored for v2.0, 06/2008; 12/2008)
 *
 */
@Component
@Scope("prototype")
public class InterfaceB_EngineBasedServer extends YHttpServlet {

    public static EngineGateway _engine;

    public static String engineId;
    public static String influxAddress;

    @Autowired
    private Property property;

    @Override
    public void init(ServletConfig config) throws ServletException {
        int maxWaitSeconds = 5;                             // a default

        try {
            ServletContext context = config.getServletContext();

            engineId=property.getEngineid();
            influxAddress=property.getInflux_address();

            // init engine reference
           
            if (InterfaceB_EngineBasedServer._engine == null) {
                String persistOn = context.getInitParameter("EnablePersistence");
                boolean persist = (persistOn != null) && persistOn.equalsIgnoreCase("true");
                String enableHbnStatsStr =
                        context.getInitParameter("EnableHibernateStatisticsGathering");
                boolean enableHbnStats = ((enableHbnStatsStr != null) &&
                        enableHbnStatsStr.equalsIgnoreCase("true"));
                InterfaceB_EngineBasedServer._engine = new EngineGatewayImpl(persist, enableHbnStats);
                InterfaceB_EngineBasedServer._engine.setActualFilePath(context.getRealPath("/"));
                context.setAttribute("engine", InterfaceB_EngineBasedServer._engine);
                // set flag to disable logging (only if false) - enabled with persistence by
                // default
                String logStr = context.getInitParameter("EnableLogging");
                if ((logStr != null) && logStr.equalsIgnoreCase("false")) {
                    InterfaceB_EngineBasedServer._engine.disableLogging();
                }

                // add the reference to the default worklist
                InterfaceB_EngineBasedServer._engine.setDefaultWorklist(property.getMaster_address());

                // set flag for generic admin account (only if set to true)
                String allowAdminID = context.getInitParameter("AllowGenericAdminID");
                if ((allowAdminID != null) && allowAdminID.equalsIgnoreCase("true")) {
                    InterfaceB_EngineBasedServer._engine.setAllowAdminID(true);
                    
                }

                // read the current version properties
                InterfaceB_EngineBasedServer._engine.initBuildProperties(context.getResourceAsStream(
                        "/WEB-INF/classes/version.properties"));
                
            }

            
            

            // set the path to external db gateway plugin classes (if any)
            String pluginPath = context.getInitParameter("ExternalPluginsPath");
            ExternalDBGatewayFactory.setExternalPaths(pluginPath);
            PredicateEvaluatorFactory.setExternalPaths(pluginPath);

            // override the max time that initialisation events wait for between
            // final engine init and server start completion
            int maxWait = StringUtil.strToInt(
                    context.getInitParameter("InitialisationAnnouncementTimeout"), -1);
            if (maxWait >= 0) maxWaitSeconds = maxWait;

            

            // init any 3rd party observer gateways
            String gatewayStr = context.getInitParameter("ObserverGateway");
            if (gatewayStr != null) {

                // split multiples on the semi-colon (if any)
                for (String gateway : gatewayStr.split(";")) {
                    registerObserverGateway(gateway);
                }
            }
        }
        catch (YPersistenceException e) {
            _log.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }

        if (InterfaceB_EngineBasedServer._engine != null) {
            InterfaceB_EngineBasedServer._engine.notifyServletInitialisationComplete(maxWaitSeconds);
        }
        else {
            _log.fatal("Failed to initialise Engine (unspecified failure). Please " +
                    "consult the logs for details");
            throw new UnavailableException("Unspecified engine failure");
        }
    }




    private void registerObserverGateway(String gatewayClassName) {
        ObserverGateway gateway ;
        try {
            Class gatewayClass = Class.forName(gatewayClassName);

            // If the class has a getInstance() method, call that method rather than
            // calling a constructor (& thus instantiating 2 instances of the class)
            try {
                Method instMethod = gatewayClass.getDeclaredMethod("getInstance");
                gateway = (ObserverGateway) instMethod.invoke(null);
            }

            // no getInstance(), so just create a plain new instance
            catch (NoSuchMethodException nsme) {
                gateway = (ObserverGateway) gatewayClass.newInstance();
            }

            if (gateway != null)
                InterfaceB_EngineBasedServer._engine.registerObserverGateway(gateway);
            else
                _log.warn("Error registering external ObserverGateway '{}'.",
                        gatewayClassName);
        }
        catch (ClassNotFoundException e) {
            _log.warn("Unable to locate external ObserverGateway '" +
                    gatewayClassName + "'.", e);
        }
        catch (InstantiationException ie) {
            _log.warn("Unable to instantiate external ObserverGateway '" +
                    gatewayClassName +
                    "'. Perhaps it is missing a no-argument constructor.", ie);
        }
        catch (YAWLException ye) {
            _log.warn("Failed to register external ObserverGateway '" +
                    gatewayClassName + "'.", ye);
        }
        catch (Exception e) {
            _log.warn("Unable to instantiate external ObserverGateway '" +
                    gatewayClassName + "'.", e);
        }
    }

    public void destroy() {
        InterfaceB_EngineBasedServer._engine.shutdown();
        super.destroy();
    }


    public EngineGateway getEngine(){
        return InterfaceB_EngineBasedServer._engine;
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);                 // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");

        Task task=new Task(request);
        Dispatcher.addTask(task);


        synchronized (task){
            try {
                while (task.getResult()==null){
                    task.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        output.append(task.getResult());

        output.append("</response>");
        if (InterfaceB_EngineBasedServer._engine.enginePersistenceFailure())
        {
            _log.fatal("************************************************************");
            _log.fatal("A failure has occurred whilst persisting workflow state to the");
            _log.fatal("database. Check the status of the database connection defined");
            _log.fatal("for the YAWL service, and restart the YAWL web application.");
            _log.fatal("Further information may be found within the Tomcat log files.");
            _log.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
        //todo find out how to provide a meaningful 500 message in the format of  a fault message.
    }


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################




    private void debug(HttpServletRequest request, String service) {
        if (_log.isDebugEnabled()) {
            _log.debug("\nInterfaceBInterfaceB_EngineBasedServer._engineBasedServer::do{}() request.getRequestURL={}",
                    service, request.getRequestURL());
            _log.debug("\nInterfaceBInterfaceB_EngineBasedServer._engineBasedServer::do{}() request.parameters:", service);
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                _log.debug("\trequest.getParameter({}) = {}", name, request.getParameter(name));
            }
        }


    }



}
