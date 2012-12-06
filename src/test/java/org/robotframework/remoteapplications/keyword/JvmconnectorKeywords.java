package org.robotframework.remoteapplications.keyword;


import java.io.File;

import org.apache.commons.io.FileUtils;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.library.AnnotationLibrary;
import org.robotframework.remoteapplications.mocks.MyApplication;


@RobotKeywords
public class JvmconnectorKeywords extends AnnotationLibrary {
    private final String tmpDir = System.getProperty("java.io.tmpdir");
    
    public JvmconnectorKeywords() {
        super("org/robotframework/remoteapplications/keyword/**/*.class");
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
    public void startAnotherInstance(String applicationArgs, String jvmArgs) {
        MyApplication.startAnotherInstance(applicationArgs, jvmArgs);
    }
}