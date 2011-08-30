package org.robotframework.jvmconnector.util;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.springframework.remoting.rmi.RmiServiceExporter;

public class MyRmiServiceExporter extends RmiServiceExporter {
	private Registry myReg;

	protected Registry getRegistry(int port) throws RemoteException {
		if (myReg == null) {
			myReg = super.getRegistry(port);
			return myReg;
		}
		return super.getRegistry(port);
	}
	
	public void destroy() throws RemoteException {
		super.destroy();
		UnicastRemoteObject.unexportObject(myReg, true);
	}
}
