package edu.sysu.yawl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.yawlfoundation.yawl.engine.interfce.InterfaceC.AddTenantRequestThread;
import org.yawlfoundation.yawl.engine.interfce.InterfaceC.ExecuteThread;
import org.yawlfoundation.yawl.engine.interfce.InterfaceC.TenantPriortyManagement;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EngineBasedServer;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.Dispatcher;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedServer;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.Task;
import org.yawlfoundation.yawl.util.CharsetFilter;

@SpringBootApplication
@EnableConfigurationProperties(Property.class)
public class SpringbootYawlApplication {

	public static void main(String[] args) {
		Logger logger= LoggerFactory.getLogger(SpringbootYawlApplication.class);
		new Thread(() -> {
            for(;;){
              //  logger.info("running");
                try {
                    Task task= Dispatcher.taskQueue.take();
                    Dispatcher.service.submit(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

		SpringApplication.run(SpringbootYawlApplication.class, args);
	}



	@Bean
	public FilterRegistrationBean someFilterRegistration() {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CharsetFilter());
		registration.addUrlPatterns("/yawl/*");
		registration.addInitParameter("requestEncoding", "UTF-8");
		registration.setName("CharsetFilter");
		//registration.setOrder(1);
		return registration;
	}

	@Bean
	@Scope("prototype")
	InterfaceB_EngineBasedServer interfaceB_engineBasedServer(){
		return new InterfaceB_EngineBasedServer();
	}
	@Bean
	public ServletRegistrationBean interfaceBServletRegistration(){
		ServletRegistrationBean servletRegistrationBean=
				new ServletRegistrationBean(
						interfaceB_engineBasedServer(),"/yawl/ib/*"
				);
		servletRegistrationBean.setOrder(1);
		return servletRegistrationBean;
	}

	@Bean
	public ServletRegistrationBean interfaceAServletRegistration(){
		ServletRegistrationBean servletRegistrationBean=
				new ServletRegistrationBean(
						new InterfaceA_EngineBasedServer(),"/yawl/ia/*"
				);
		servletRegistrationBean.setOrder(2);
		return servletRegistrationBean;
	}
}
