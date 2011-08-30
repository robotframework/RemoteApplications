package org.robotframework.jvmconnector.mocks;

public class MockException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public MockException() {}
	public MockException(String message) {
		super(message);
	}
}
