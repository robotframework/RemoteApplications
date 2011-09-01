package org.robotframework.jvmconnector.launch.jnlp;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class JarImplSpec extends Specification<JarImpl> {
    public class Any {
        private JarFile jarFile;

        public JarImpl create() {
            jarFile = mock(JarFile.class);
            return new JarImpl(jarFile);
        }
        
        public void getsMainClass() throws Exception {
            final Manifest manifest = mock(Manifest.class);
            final Attributes mainAttributes = mock(Attributes.class);
            final String mainClass = "com.acme.Foo";
            
            checking(new Expectations() {{
                one(jarFile).getManifest(); will(returnValue(manifest));
                one(manifest).getMainAttributes(); will(returnValue(mainAttributes));
                one(mainAttributes).getValue("Main-Class"); will(returnValue(mainClass));
            }});
            
            specify(context.getMainClass(), mainClass);
        }
        
        public void getsPath() {
            checking(new Expectations() {{
                one(jarFile).getName(); will(returnValue("/tmp/someJar.jar"));
            }});
            
            specify(context.getPath(), "/tmp/someJar.jar");
        }
    }
}
