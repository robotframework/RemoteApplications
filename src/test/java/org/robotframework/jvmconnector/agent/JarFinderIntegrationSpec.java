package org.robotframework.jvmconnector.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import jdave.junit4.JDaveRunner;

import org.junit.runner.RunWith;
import org.robotframework.jdave.mock.MockSupportSpecification;

@RunWith(JDaveRunner.class)
public class JarFinderIntegrationSpec extends MockSupportSpecification<String> {
    public class HandlingDirectory {
        public String create() {
            return JarSpecUtil.jarDir;
        }

        public void findsJars() {
            List<String> expectedJars = findJars(context);
            specify(expectedJars, containsExactly("helper-keywords.jar", "jvmconnector.jar", "swinglibrary.jar"));
        }
    }
    
    public class HandlingFile {
        public String create() {
            return JarSpecUtil.jarDir + JarSpecUtil.fileSep + "helper-keywords.jar";
        }

        public void findsJars() {
            List<String> expectedJars = findJars(context); 
            specify(expectedJars, containsExactly("helper-keywords.jar"));
        }
    }

    private List<String> findJars(String file) {
        final List<String> jarsFound = new ArrayList<String>();
        JarFinder jarFinder = new JarFinder(file);
        jarFinder.each(new JarFileAction() {
            public void doOnFile(JarFile jar) {
                int lastFileSepIndex = jar.getName().lastIndexOf(JarSpecUtil.fileSep);
                String simpleName = jar.getName().substring(lastFileSepIndex + 1);
                jarsFound.add(simpleName);
            }
        });
        return jarsFound;
    }
}
