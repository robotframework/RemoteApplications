package org.robotframework.jvmconnector.mocks;

import org.robotframework.jvmconnector.xmlrpc.RobotXmlRpcServer;

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
