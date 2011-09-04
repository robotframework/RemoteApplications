package org.robotframework.remoteapplications.xmlrpc;

import org.robotframework.remoteapplications.mocks.MockJavaLibrary;

public class XmlRpcStarter {
    public void startXmlRpcServer() throws Exception {
        RobotXmlRpcServer server = new RobotXmlRpcServer(new MockJavaLibrary());
        server.startServer();
    }
    
    public static void main(String[] args) throws Exception {
        new XmlRpcStarter().startXmlRpcServer();
    }
}
