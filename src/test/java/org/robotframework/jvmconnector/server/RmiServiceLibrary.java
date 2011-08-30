package org.robotframework.jvmconnector.server;

import org.robotframework.jvmconnector.util.RmiHelperUtil;
import org.springframework.context.support.GenericApplicationContext;


public class RmiServiceLibrary {
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
    private int port = 1099;
    private GenericApplicationContext serverApplicationContext; 
	
	public RmiServiceLibrary() { }
	
	public RmiServiceLibrary(String port) {
		this.port = Integer.parseInt(port);
	}
	
	public void startRmiService() {
	    serverApplicationContext = RmiHelperUtil.getServerApplicationContext(port);
		serverApplicationContext.refresh();
	}
	
	public void closeRmiService() {
	    serverApplicationContext.close();
	}
}
