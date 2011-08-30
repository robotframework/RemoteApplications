package org.robotframework.jvmconnector.mocks;

import java.io.IOException;

public class MyApplication {
    public static boolean isRunning = false;
    public static String[] args;
    
    public static void main(String[] args) throws Exception {
    	if (args.length == 1 && args[0].equals("log")) {
    		startLogging();
    	}
    	
        MyApplication.args = args;
        isRunning = true;
        Thread.sleep(500);
    }
    
    private static void startLogging() throws Exception {
    	final String msg = "JvmConnector is a module that enables remote keyword invocation.";
    	final String errors = "error";
    	
    	new Thread() {
    		public void run() {
    			while(true) {
    				System.out.println(msg);
    				System.err.println(errors);
    				try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}.start();
    	Thread.sleep(10000);
	}

	public static void startAnotherInstance(String applicationArgs, String jvmArgs) {
        try {
            Runtime.getRuntime().exec("java " + jvmArgs + " " + MyApplication.class.getName() + " " + applicationArgs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
