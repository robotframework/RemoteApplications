/*
 * Copyright 2008-2011 Nokia Siemens Networks Oyj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.jvmconnector.xmlrpc;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.robotframework.javalib.library.RobotJavaLibrary;

public class RobotXmlRpcServer {
    private final RobotLibrary library;
    private final WebServer webServer;
    
    public RobotXmlRpcServer(RobotJavaLibrary library) {
        this(library, 8270);    
    }
    
    public RobotXmlRpcServer(RobotJavaLibrary library, int port) {
        this.library = new CloseableLibraryDecorator(library);
        this.webServer = new WebServer(port);
    }
    
    public void startServer() throws Exception {
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        setHandlers(xmlRpcServer);
        webServer.start();
        System.out.println("XMLRPC Server up and running...");
    }

    private void setHandlers(XmlRpcServer xmlRpcServer) {
        xmlRpcServer.setHandlerMapping(new XmlRpcHandlerMapping() {
            @SuppressWarnings("serial")
            private final Map<String, XmlRpcHandler> handlers = new HashMap<String, XmlRpcHandler>() {{ 
                put("get_keyword_names", new GetKeywordNameHandler(library)); 
                put("run_keyword", new RunKeywordHandler(library));
                put("get_keyword_arguments", new GetKeywordArgumentsHandler(library));
                put("get_keyword_documentation", new GetKeywordDocumentationHandler(library));
            }};
            
            public XmlRpcHandler getHandler(String handlerName) throws XmlRpcNoSuchHandlerException, XmlRpcException {
                return handlers.get(handlerName);
            }
        });
    }
    
    public void shutdownServer() {
        webServer.shutdown();
    }
}

