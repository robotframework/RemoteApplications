package org.robotframework.jvmconnector.agent;

import java.io.File;
import java.util.jar.JarFile;

public class JarSpecUtil {
    static final String fileSep = System.getProperty("file.separator");
    static final String pathSep = System.getProperty("path.separator");
    static final String thisDir = new File(".").getAbsolutePath();
    static final String jarDir =  thisDir + fileSep + "src" + fileSep + "test" + fileSep + "resources" + fileSep + "test-lib";
    static String getSimpleName(JarFile jar) {
        int lastFileSepIndex = jar.getName().lastIndexOf(JarSpecUtil.fileSep);
        return jar.getName().substring(lastFileSepIndex + 1);
    }
}
