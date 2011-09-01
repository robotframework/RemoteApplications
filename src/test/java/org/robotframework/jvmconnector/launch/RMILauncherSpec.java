package org.robotframework.jvmconnector.launch;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.laughingpanda.beaninject.Inject;
import org.robotframework.jvmconnector.server.ApplicationLauncher;
import org.robotframework.jvmconnector.server.RmiService;

@RunWith(JDaveRunner.class)
public class RMILauncherSpec extends Specification<RMILauncher> {
    public class WhenExecuted {
        private String applicationClassName = "com.acme.SomeApp";
        private String pathToStorage = "/tmp/launcher.txt";
        private RmiService rmiService;
        private ApplicationLauncher applicationLauncher;

        public void create() {
            rmiService = injectMock(RmiService.class, "rmiService", RMILauncher.class);
            applicationLauncher = injectMock(ApplicationLauncher.class, "applicationLauncher", RMILauncher.class);
        }
        
        public void startsRmiService() throws Exception {
            checking(new Expectations() {{
                one(rmiService).start(pathToStorage);
                ignoring(applicationLauncher);
            }});
            
            RMILauncher.main(new String[] {pathToStorage, applicationClassName });
        }
        
        public void startsApplication() throws Exception {
            checking(new Expectations() {{
                one(applicationLauncher).launchApplication(applicationClassName, new String[] {"foo", "bar"});
                ignoring(rmiService);
            }});
            
            RMILauncher.main(new String[] {pathToStorage, applicationClassName, "foo", "bar"});
        }
        
        private <T> T injectMock(Class<T> mockedClass, String fieldName, Class<?> target) {
            T mockedDependency = mock(mockedClass);
            Inject.staticField(fieldName).of(target).with(mockedDependency);
            return mockedDependency;
        }
    }
}
