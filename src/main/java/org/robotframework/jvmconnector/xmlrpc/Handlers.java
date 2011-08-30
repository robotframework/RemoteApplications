package org.robotframework.jvmconnector.xmlrpc;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.javalib.util.StdStreamRedirecter;

class GetKeywordNameHandler implements XmlRpcHandler {
    private final RobotJavaLibrary library;
    
    public GetKeywordNameHandler(RobotJavaLibrary library) {
        this.library = library;
    }

    public Object execute(XmlRpcRequest req) throws XmlRpcException {
        return library.getKeywordNames();
    }
}

class RunKeywordHandler implements XmlRpcHandler {
    private final RobotJavaLibrary library;
    private StdStreamRedirecter outStreamRedirecter;

    public RunKeywordHandler(RobotJavaLibrary library) {
        this.library = library;
    }

    public Object execute(XmlRpcRequest req) throws XmlRpcException {
        redirectOutputStreams();
        Map<String, String> rslt = null;
        try {
            rslt = runKeyword(req);
        } catch (final Throwable t) {
            rslt = failKeywordRunning(outStreamRedirecter, t);
        } finally {
            rslt.put("output", outStreamRedirecter.getStdOutAsString() + "\n" + outStreamRedirecter.getStdErrAsString());
            resetOutputStreams(outStreamRedirecter);
        }
        return rslt;
    }

    private void redirectOutputStreams() {
        outStreamRedirecter = new StdStreamRedirecter();
        outStreamRedirecter.redirectStdStreams();
    }

    @SuppressWarnings("serial")
    private Map<String, String> runKeyword(XmlRpcRequest req) {
        String keywordName = (String)req.getParameter(0);            
        Object[] args = (Object[])req.getParameter(1);
        final Object rslt = library.runKeyword(keywordName, args);
        return new HashMap<String, String>() {{
            put("status", "PASS");
            put("return", ""+rslt);
        }};
    }
    
    @SuppressWarnings("serial")
    private Map<String, String> failKeywordRunning(final StdStreamRedirecter outStreamRedirecter, final Throwable t) {
        return new HashMap<String, String>() {{
            put("status", "FAIL");
            put("error", t.getMessage());
            put("traceback", extractStackTrace(t));
        }};
    }

    private String extractStackTrace(final Throwable t) {
        final StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement elem : t.getStackTrace())
            stackTrace.append(elem).append("\n");
        return stackTrace.toString();
    }

    private void resetOutputStreams(StdStreamRedirecter outStreamRedirecter) {
        outStreamRedirecter.resetStdStreams();
    }
}

class GetKeywordArgumentsHandler implements XmlRpcHandler {
    private final RobotLibrary library;

    public GetKeywordArgumentsHandler(RobotLibrary library) {
        this.library = library;
    }

    public Object execute(XmlRpcRequest req) throws XmlRpcException {
        String keywordName = (String)req.getParameter(0);
        try {
            return library.getKeywordArguments(keywordName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

class GetKeywordDocumentationHandler implements XmlRpcHandler {
    private final RobotLibrary library;

    public GetKeywordDocumentationHandler(RobotLibrary library) {
        this.library = library;
    }

    public Object execute(XmlRpcRequest req) throws XmlRpcException {
        String keywordName = (String)req.getParameter(0);
        return library.getKeywordDocumentation(keywordName);
    }
}