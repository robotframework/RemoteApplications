package org.robotframework.jvmconnector.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.robotframework.jvmconnector.mocks.ExceptionThrowingKeyword;
import org.robotframework.jvmconnector.mocks.LoggingKeyword;
import org.robotframework.jvmconnector.mocks.MockJavaLibrary;
import org.robotframework.jvmconnector.util.RmiHelperUtil;
import org.robotframework.jvmconnector.util.RmiHelperUtil.FakeRmiClient;
import org.springframework.context.support.GenericApplicationContext;

public class RobotRmiIntegrationTest extends TestCase {
    private GenericApplicationContext serverAppCtx;
    private GenericApplicationContext clientBeanFactory;
    private FakeRmiClient rmiClient;

    public RobotRmiIntegrationTest() {
        int freePort = RmiHelperUtil.getFreePort();
        serverAppCtx = RmiHelperUtil.getServerApplicationContext(freePort);
        clientBeanFactory = RmiHelperUtil.getClientBeanFactory(freePort);
    }

    public void setUp() {
        serverAppCtx.refresh();
        rmiClient = new FakeRmiClient(clientBeanFactory);
    }

    public void tearDown() throws Exception {
        serverAppCtx.close();
        serverAppCtx.destroy();
    }

    public void testClientFindsKeywordsFromTheLibraryInAppContext() {
        String[] keywords = rmiClient.getKeywordNames();
        assertArraysContainSame(new MockJavaLibrary().getKeywordNames(), keywords);
    }

    public void testClientRunsKeywordFromTheLibrary() {
        Object keywordRetVal = rmiClient.runKeyword(LoggingKeyword.KEYWORD_NAME, null);
        assertEquals(LoggingKeyword.RETURN_VALUE, keywordRetVal);
    }

    public void testPrintsKeywordStdOutLoggingToStdOut() {
        rmiClient.runKeyword(LoggingKeyword.KEYWORD_NAME, null);
        assertEquals(LoggingKeyword.LOG_STRING_STDOUT, rmiClient.getStdOutAsString());
    }

    public void testPrintsKeywordStdErrLoggingToStdErr() {
        rmiClient.runKeyword(LoggingKeyword.KEYWORD_NAME, null);
        assertEquals(LoggingKeyword.LOG_STRING_STDERR, rmiClient.getStdErrAsString());
    }

    public void testExceptionReturnedByKeywordExecutionResultIsThrown() {
        try {
            rmiClient.runKeyword(ExceptionThrowingKeyword.KEYWORD_NAME, null);
            fail("Excpected testFailedException to be thrown");
        } catch (TestFailedException e) {}
    }

    private void assertArraysContainSame(Object[] expectedAr, Object[] ar) {
        Set expectedContents = new HashSet(Arrays.asList(expectedAr));
        Set contents = new HashSet(Arrays.asList(ar));
        assertEquals(expectedContents, contents);
    }
}
