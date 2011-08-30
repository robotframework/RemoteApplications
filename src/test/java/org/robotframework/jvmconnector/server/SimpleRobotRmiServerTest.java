package org.robotframework.jvmconnector.server;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.javalib.util.StdStreamRedirecter;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;
import org.robotframework.jvmconnector.mocks.ExceptionThrowingKeyword;
import org.robotframework.jvmconnector.mocks.LoggingKeyword;
import org.robotframework.jvmconnector.mocks.MockException;
import org.robotframework.jvmconnector.mocks.MockJavaLibrary;

public class SimpleRobotRmiServerTest extends MockObjectTestCase {
    private String keywordName = "some keyword";
    private Object[] keywordArguments = new Object[0];
    private Object keywordReturnValue = new Object();
    
	private Mock mockJavaLibrary;
	private Mock mockStreamRedirecter;
	private SimpleRobotRmiService robotRmiService;
	
	public void setUp() throws Exception {
	    robotRmiService = createRmiServiceWithMockInternals();
	}
	
	public void testGetsKeywords() {
		String[] keywordNames = new String[0];
		mockJavaLibrary.expects(once()).method("getKeywordNames")
			.will(returnValue(keywordNames));
		
		assertEquals(keywordNames, robotRmiService.getKeywordNames());
	}
	
	public void testRunsKeywords() {
		mockJavaLibrary.expects(once()).method("runKeyword")
			.with(same(keywordName), same(keywordArguments));

		robotRmiService.runKeyword(keywordName, keywordArguments);
	}
	
	public void testWrapsResults() {
		mockJavaLibrary.stubs().method("runKeyword")
			.will(returnValue(keywordReturnValue));
			
		KeywordExecutionResult keywordExecutionResult = 
			robotRmiService.runKeyword(keywordName, keywordArguments);
		
		assertEquals(keywordReturnValue, keywordExecutionResult.getResult());
	}
	
	public void testRetainsSystemOutLogWhenFails() {
		KeywordExecutionResult keywordExecutionResults = 
			executeMockKeyword(ExceptionThrowingKeyword.KEYWORD_NAME);
		
		assertEquals(ExceptionThrowingKeyword.LOG_STRING_STDOUT, keywordExecutionResults.getStdOutAsString());
	}
	
	public void testRetainsSystemErrLogWhenFails() {
		KeywordExecutionResult keywordExecutionResults = 
			executeMockKeyword(ExceptionThrowingKeyword.KEYWORD_NAME);
		
		assertEquals(ExceptionThrowingKeyword.LOG_STRING_STDERR, keywordExecutionResults.getStdErrAsString());
	}
	
	public void testPassesWhenKeywordPasses() {
		mockJavaLibrary.stubs().method("runKeyword")
			.will(returnValue(new Object()));
		
		assertTrue(robotRmiService.runKeyword(keywordName, keywordArguments).keywordPassed());
	}
	
	public void testFailsWhenKeywordFails() {
		mockJavaLibrary.stubs().method("runKeyword")
			.will(throwException(new Throwable()));
		
		assertFalse(robotRmiService.runKeyword(keywordName, keywordArguments).keywordPassed());
	}
	
	public void testWrapsExceptionWhenFails() {
	    mockJavaLibrary.stubs().method("runKeyword")
	    .will(throwException(new RuntimeException()));
	    
	    KeywordExecutionResult executionResult = robotRmiService.runKeyword(keywordName, keywordArguments);
	    assertNotNull(executionResult.getTestFailedException());
	}
	
	public void testContainsNoWrappedExceptionWhenPasses() {
		KeywordExecutionResult executionResult = robotRmiService.runKeyword(keywordName, keywordArguments);
		assertTrue(executionResult.getTestFailedException() == null);
	}
	
	public void testCatchesExceptionsThrownByKeywords() {
		mockJavaLibrary.stubs().method("runKeyword")
			.will(throwException(new RuntimeException()));
		
		robotRmiService.runKeyword(keywordName, keywordArguments);
	}
	
	public void testContainsSystemOutLog() {
		KeywordExecutionResult keywordExecutionResults = executeMockKeyword(LoggingKeyword.KEYWORD_NAME);
		assertEquals(LoggingKeyword.LOG_STRING_STDOUT, keywordExecutionResults.getStdOutAsString());
	}
	
	public void testContainsSystemErrLog() {
		KeywordExecutionResult keywordExecutionResults = executeMockKeyword(LoggingKeyword.KEYWORD_NAME);
		assertEquals(LoggingKeyword.LOG_STRING_STDERR, keywordExecutionResults.getStdErrAsString());
	}
	
	public void testClearsLogsAfterPassingKeyword() {
		mockJavaLibrary.expects(once()).method("runKeyword").id("runKwd");
		mockStreamRedirecter.expects(once()).method("resetStdStreams").after(mockJavaLibrary, "runKwd");
		
		robotRmiService.runKeyword(keywordName, keywordArguments);
	}

	public void testClearsLogsAfterFailingPassword() {
		mockJavaLibrary.expects(once()).method("runKeyword")
			.will(throwException(new RuntimeException()));
		mockStreamRedirecter.expects(once()).method("resetStdStreams");

		robotRmiService.runKeyword(keywordName, keywordArguments);
	}
	
	public void testWrapsExceptions() {
		mockJavaLibrary.expects(once()).method("runKeyword")
			.will(throwException(new MockException()));
		
		KeywordExecutionResult executionResult = robotRmiService.runKeyword(keywordName, keywordArguments);
		assertEquals(MockException.class.getName(), executionResult.getTestFailedException().getSourceExceptionClassName());
	}
	
	private KeywordExecutionResult executeMockKeyword(String mockKeywordName) {
		SimpleRobotRmiService tmpRmiService = new SimpleRobotRmiService();
		tmpRmiService.setLibrary(new MockJavaLibrary());
		return tmpRmiService.runKeyword(mockKeywordName, null);
	}
	
    private SimpleRobotRmiService createRmiServiceWithMockInternals() {
        StdStreamRedirecter redirecterStub = createStreamRedirecterStub();
        RobotJavaLibrary libraryStub = createRobotJavaLibraryStub();
        return createRobotRmiService(libraryStub, redirecterStub);
    }
    
	private SimpleRobotRmiService createRobotRmiService(RobotJavaLibrary library, StdStreamRedirecter streamRedirecter) {
		robotRmiService = new SimpleRobotRmiService(streamRedirecter);
		robotRmiService.setLibrary(library);
		return robotRmiService;
	}
	
	private StdStreamRedirecter createStreamRedirecterStub() {
		mockStreamRedirecter = mock(StdStreamRedirecter.class);
		mockStreamRedirecter.stubs();
		return (StdStreamRedirecter) mockStreamRedirecter.proxy();
	}
	
	private RobotJavaLibrary createRobotJavaLibraryStub() {
		mockJavaLibrary = mock(RobotJavaLibrary.class);
		mockJavaLibrary.stubs();
		return (RobotJavaLibrary) mockJavaLibrary.proxy();
	}
}
