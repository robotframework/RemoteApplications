package org.robotframework.jvmconnector.server;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.robotframework.javalib.util.StdStreamRedirecter;
import org.robotframework.jvmconnector.common.TestFailedException;


public class KeywordExecutionResultImplTest extends MockObjectTestCase {
	private KeywordExecutionResultImpl executionResultImpl;

	public void setUp() {
		executionResultImpl = new KeywordExecutionResultImpl();
	}
	
	public void testGetsStandardStreamsFromStdStreamRedirecter() {
		Mock mockStdStreamRedirecter = mock(StdStreamRedirecter.class);
		mockStdStreamRedirecter.expects(once()).method("getStdOutAsString");
		mockStdStreamRedirecter.expects(once()).method("getStdErrAsString");
		
		executionResultImpl.setStdStreams((StdStreamRedirecter) mockStdStreamRedirecter.proxy());
	}
	
	public void testStatusIsPassed() {
		assertTrue(executionResultImpl.keywordPassed());
	}
	
	public void testSettingKeywordFaileExceptionSetsTheStatusToFailed() {
		executionResultImpl.setTestFailedException(new TestFailedException(new Exception()));
		assertFalse(executionResultImpl.keywordPassed());
	}
}
