package org.robotframework.jvmconnector.util;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;

public class FileServer {
    public static final int HTTP_PORT = 14563;
    public static final int HTTPS_PORT = 14564;
    public static final String URL_BASE = "http://localhost:" + HTTP_PORT;
    private static Server server;
    private static String resourceBase;

    public static void start(String resourceBase) throws Exception {
        FileServer.resourceBase = resourceBase;
        if (isStarted()) return;
        server = new Server(HTTP_PORT);
        server.setConnectors(new Connector[] {getPlainConnector(HTTP_PORT), getSslConnector(HTTPS_PORT)});
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(resourceBase);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler() });
        server.setHandler(handlers);
        server.start();
    }

    public static void stop() throws Exception {
        try {
            server.destroy(); // calling stop() would make maven hang
        } catch (Exception e) {
            // Ignored intentionally
        }
    }
    
    public static void main(String[] args) throws Exception {
        String resourceBase = new File("./src/test/resources").getCanonicalPath();
        System.out.println(resourceBase);
        start(resourceBase);
    }
    
    private static SelectChannelConnector getPlainConnector(int port) {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        return connector;
    }
    
    private static SslSocketConnector getSslConnector(int port) {
        SslSocketConnector sslConnector = new SslSocketConnector();
        sslConnector.setKeystore(resourceBase + "/keystore");
        sslConnector.setKeyPassword("pulkkisenjorma");
        sslConnector.setPort(port);
        sslConnector.setMaxIdleTime(30000);
        return sslConnector;
    }
    
    private static boolean isStarted() {
        return server != null && server.isStarted();
    }
}



