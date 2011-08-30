package org.robotframework.jvmconnector.agent;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class Java6ClassPathAppenderSpec extends Specification<Java6ClassPathAppender> {
    public class Any {
        private Instrumentation mustangInstrumentation;
        private Java6ClassPathAppender classPathAppender;

        public Java6ClassPathAppender create() {
            mustangInstrumentation = mock(Instrumentation.class);
            
            classPathAppender = new Java6ClassPathAppender(mustangInstrumentation);
            return classPathAppender;
        }
        
        public void appendsToClasspathWithMustang() {
            final JarFile jarFile = dummy(JarFile.class);
            checking(new Expectations() {{
                one(mustangInstrumentation).appendToSystemClassLoaderSearch(jarFile);
            }});
            
            classPathAppender.appendToClasspath(jarFile);
        }
    }
}
