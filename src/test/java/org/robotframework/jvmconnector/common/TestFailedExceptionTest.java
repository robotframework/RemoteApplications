package org.robotframework.jvmconnector.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.robotframework.jvmconnector.mocks.MockException;
import org.robotframework.jvmconnector.util.ThrowableUtil;


public class TestFailedExceptionTest extends TestCase {
	private String exceptionMessage = "Exception message";
	private MockException mockException;
	private TestFailedException testFailedException;

	public void setUp() {
		mockException = new MockException(exceptionMessage);
		testFailedException = new TestFailedException(mockException);
	}
	
	public void testContainsSourceExceptionsStacktrace() {
		assertPrintStackTraceEquals(mockException, testFailedException);
	}
	
	public void testMessageContainsSourceExceptionsClassName() {
		assertContains(MockException.class.getName(), testFailedException.getMessage());
	}
	
	public void testMessageContainsSourceExceptionsMessage() {
		assertContains(mockException.getMessage(), testFailedException.getMessage());
	}
	
	public void testMessageDoesnContainSourceExceptionMessageIfNull() {
		MockException mockExceptionWithoutMessage = new MockException();
		
		try {
			throw mockExceptionWithoutMessage;
		} catch (MockException e) {
			testFailedException = new TestFailedException(e);
		}
		
		assertDoesntContain("null", testFailedException.getMessage());
	}
	
	public void testGetSourceClassNameReturnsSourceClassName() {
		assertEquals(MockException.class.getName(), testFailedException.getSourceExceptionClassName());
	}
	
	public void testContainsWholeThrowableHierarchy() {
		Throwable throwableWithHierarchy = ThrowableUtil.getThrowableHierarchy(); 
		TestFailedException keywordExceptionWithHierarchy =
			new TestFailedException(throwableWithHierarchy);
		
		assertPrintStackTraceEquals(throwableWithHierarchy,
			keywordExceptionWithHierarchy);
	}
	
	private void assertDoesntContain(String contained, String container) {
		assertTrue(container.indexOf(contained) < 0);
	}

	private void assertContains(String contained, String container) {
		assertTrue(container.indexOf(contained) >= 0);
	}

	private void assertPrintStackTraceEquals(Throwable expectedThrowable, Throwable throwable) {
		assertPrintStackTraceWithPrintStreamEquals(expectedThrowable, throwable);
		assertPrintStackTraceWithPrintWriterEquals(expectedThrowable, throwable);
	}

	private void assertPrintStackTraceWithPrintWriterEquals(Throwable expectedThrowable, Throwable actual) {
		String expectedOutput = getStackTraceWithPrintWriterAsString(expectedThrowable);
		String actualOutput = getStackTraceWithPrintWriterAsString(actual);
		
		assertEquals(expectedOutput, actualOutput);
	}

	private void assertPrintStackTraceWithPrintStreamEquals(Throwable expectedThrowable, Throwable actualThrowable) {
		String expectedOutput = getStackTraceWithPrintStreamAsString(expectedThrowable);
		String actualOutput = getStackTraceWithPrintStreamAsString(actualThrowable);
		assertEquals(expectedOutput, actualOutput);
	}
	
	private String getStackTraceWithPrintWriterAsString(Throwable throwable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(output);
		throwable.printStackTrace(printWriter);
		printWriter.close();
		return output.toString();
	}

	private String getStackTraceWithPrintStreamAsString(Throwable expectedThrowable) {
		ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(expectedOutput);
		expectedThrowable.printStackTrace(printStream);
		printStream.close();
		return expectedOutput.toString();
	}
}
