package org.robotframework.remoteapplications.mocks;

import org.robotframework.remoteapplications.xmlrpc.RobotXmlRpcServer;

public class XmlRpcTestApplication {
    public static void main(String[] args) throws Exception {
        RobotXmlRpcServer server = new RobotXmlRpcServer(new MockJavaLibrary());
        server.startServer();
        
        XmlRpcTestApplication app = new XmlRpcTestApplication();
        app.spendTime();
    }
    
    private void spendTime() {
        while (true) {}
    }
}
