package org.robotframework.jvmconnector.keyword;


import java.io.File;

import org.apache.commons.io.FileUtils;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.library.AnnotationLibrary;
import org.robotframework.jvmconnector.launch.jnlp.JnlpEnhancer;
import org.robotframework.jvmconnector.mocks.MyApplication;


@RobotKeywords
public class JvmconnectorKeywords extends AnnotationLibrary {
    private final String tmpDir = System.getProperty("java.io.tmpdir");
    
    public JvmconnectorKeywords() {
        super("org/robotframework/jvmconnector/keyword/**/*.class");
    }
    
    @RobotKeyword
    public boolean applicationIsRunning() {
        return MyApplication.isRunning;
    }
    
    @RobotKeyword
    public void stopApplication() {
        MyApplication.isRunning = false;
    }
    
    @RobotKeyword
    public String[] getArguments() {
        return MyApplication.args;
    }
    
    @RobotKeyword
    public void stopJvm() {
        System.exit(0);
    }
    
    @RobotKeyword
    public String getEnhancedJnlp(String libraryResourceDir, String jnlpUrl) throws Exception {
        JnlpEnhancer jnlpRunner = new JnlpEnhancer(libraryResourceDir);
        String pathToJnlp = jnlpRunner.createRmiEnhancedJnlp(jnlpUrl);
        return FileUtils.readFileToString(new File(pathToJnlp), "UTF-8");
    }
    
    @RobotKeyword
    public void startAnotherInstance(String applicationArgs, String jvmArgs) {
        MyApplication.startAnotherInstance(applicationArgs, jvmArgs);
    }
}