package org.robotframework.jvmconnector.server;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.laughingpanda.beaninject.Inject;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;

@RunWith(JDaveRunner.class)
public class CloseableRobotRmiServiceSpec extends Specification<CloseableRobotRmiService> {
    private RobotRmiService wrappedService;
    public class Any {
        public CloseableRobotRmiService create() {
            wrappedService = mock(RobotRmiService.class);
            return new CloseableRobotRmiService(wrappedService);
        }
        
        public void hasWrappedKeywords() {
            checking(new Expectations() {{
               one(wrappedService).getKeywordNames(); will(returnValue(new String[] { "one", "two" }));
            }});
            
            specify(context.getKeywordNames(), containsAll("one", "two"));
        }
        
        public void hasSystemExitKeyword() {
            checking(new Expectations() {{
                one(wrappedService).getKeywordNames(); will(returnValue(new String[] { "one", "two" }));
             }});
             
             specify(context.getKeywordNames(), containsExactly("one", "two", "systemexit"));
        }
        
        public void runsKeyword() {
            final KeywordExecutionResult results = dummy(KeywordExecutionResult.class);
            checking(new Expectations() {{
                one(wrappedService).runKeyword("someKeyword", new Object[] {"one", "two"});
                will(returnValue(results));
             }});
            
            specify(context.runKeyword("someKeyword", new Object[] {"one", "two"}), results);
        }
    }
    
    public class Exiting {
        private Runtime originalRuntime;
        private Runtime mockRuntime;
        
        public CloseableRobotRmiService create() {
            originalRuntime = Runtime.getRuntime();
            mockRuntime = mock(Runtime.class);
            injectRuntime(mockRuntime);
            Inject.staticField("currentRuntime").of(Runtime.class).with(mockRuntime);
            return new CloseableRobotRmiService(dummy(RobotRmiService.class));
        }

        public void runsSystemExit() throws Exception {
            checking(new Expectations() {{
                one(mockRuntime).exit(0);
            }});
            
            context.runKeyword("systemexit", null);
        }

        public void destroy() {
            injectRuntime(originalRuntime);
        }
        
        private void injectRuntime(Runtime runtime) {
            Inject.staticField("currentRuntime").of(Runtime.class).with(runtime);    
        }
    }
}
