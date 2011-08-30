package org.robotframework.jvmconnector.xmlrpc;

import java.io.File;

import org.robotframework.javalib.library.RobotJavaLibrary;

public class ExampleLibrary implements RobotJavaLibrary {
    public Integer countItemsInDirectory(String path) {
        return new File(path).listFiles().length;
    }

    public void stringsShouldBeEqual(String str1, String str2) {
        System.out.println("Comparing '"+str1+"' to '"+str2+"'");
        if (!str1.equals(str2)) {
            throw new RuntimeException( "Given strings are not equal" );
        }
    }

    public String[] getKeywordNames() {
        return new String[] {"countitemsindirectory", "stringsshouldbeequal"};
    }

    public static void main(String[] args) throws Exception {
        RobotXmlRpcServer server = new RobotXmlRpcServer(new ExampleLibrary(), 8270);
        server.startServer();
    }

    public Object runKeyword(String keywordName, Object[] args) {
        if ("countitemsindirectory".equals(keywordName)) {
            return countItemsInDirectory( (String)args[0] );
        } else if("stringsshouldbeequal".equals(keywordName)) {
            stringsShouldBeEqual((String)args[0], (String)args[1]);
            return null;
        } else {
            throw new RuntimeException("Unknown keyword: " + keywordName);
        }
    }
}
