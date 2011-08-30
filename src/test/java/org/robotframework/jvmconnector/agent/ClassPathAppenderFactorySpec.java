package org.robotframework.jvmconnector.agent;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class ClassPathAppenderFactorySpec extends Specification<ClassPathAppenderFactory> {
    public class Any {
        private String javaVersion;

        public ClassPathAppenderFactory create() {
            javaVersion = System.getProperty("java.version");
            return new ClassPathAppenderFactory();
        }
        
        public void createsJava6AppenderWhenJava6() {
            System.setProperty("java.version", "1.6.0_14");
            specify(context.create(null).getClass(), Java6ClassPathAppender.class);
        }
        
        public void createsNullAppenderWhenJava5() {
            System.setProperty("java.version", "foo");
            specify(context.create(null).getClass(), NullClassPathAppender.class);
        }
        
        public void destroy() {
            System.setProperty("java.version", javaVersion);
        }
    }
}
