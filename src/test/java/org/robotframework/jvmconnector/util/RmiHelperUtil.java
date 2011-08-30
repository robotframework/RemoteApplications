package org.robotframework.jvmconnector.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.robotframework.jvmconnector.client.RobotRmiClient;
import org.robotframework.jvmconnector.common.PropertyOverrider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;


public class RmiHelperUtil {
	private static final String CLIENT_CONTEXT_XML = "org/robotframework/jvmconnector/client/clientContext.xml";
	private static final String SERVER_CONTEXT_XML = "org/robotframework/jvmconnector/server/testServerContext.xml";
	
	public static int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try { socket.close(); } catch (Exception e) { /* Ignore intentionally */ }
		}
	}
	
	public static GenericApplicationContext getClientBeanFactory(int port) {
		DefaultListableBeanFactory beanFactory = getBeanFactory(CLIENT_CONTEXT_XML);
		changeBeanProperty(beanFactory, "robotRmiService.serviceUrl", "rmi://localhost:" + port + "/jvmConnector");
		return new GenericApplicationContext(beanFactory);
	}

	public static GenericApplicationContext getServerApplicationContext(int port) {
		DefaultListableBeanFactory beanFactory = getBeanFactory(SERVER_CONTEXT_XML);
		changeBeanProperty(beanFactory, "serviceExporter.registryPort", "" + port);
		return new GenericApplicationContext(beanFactory);
	}

	private static DefaultListableBeanFactory getBeanFactory(String resourcePath) {
		return new XmlBeanFactory(new ClassPathResource(resourcePath));
	}

	private static void changeBeanProperty(ConfigurableListableBeanFactory beanFactory, String beanPropertyName,
		String propertyValue) {
		PropertyOverrider propertyOverrider = new PropertyOverrider();
		propertyOverrider.addOverridableProperty(beanPropertyName, propertyValue);
		propertyOverrider.postProcessBeanFactory(beanFactory);
	}
	
	public static class FakeRmiClient extends RobotRmiClient {
		private StringBuffer stdOutBuffer = new StringBuffer();
		private StringBuffer stdErrBuffer = new StringBuffer();
		
		public FakeRmiClient(BeanFactory beanFactory) {
			super(beanFactory);
		}
		
		public String getStdOutAsString() {
			return stdOutBuffer.toString();
		}
		
		public String getStdErrAsString() {
			return stdErrBuffer.toString();
		}
		
		protected void printStdOut(String stdOutAsString) {
			stdOutBuffer.append(stdOutAsString);
		}
		
		protected void printStdErr(String stdErrAsString) {
			stdErrBuffer.append(stdErrAsString);
		}
	}
}
