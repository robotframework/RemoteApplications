package org.robotframework.jvmconnector.util;

import org.robotframework.jvmconnector.mocks.MockException;

public class ThrowableUtil {
	public static StackTraceElement[] getStackTrace() {
		return getThrowableHierarchy().getStackTrace();
	}
	
	public static Throwable getThrowableHierarchy() {
		return new Throwable(new Exception(new MockException("cause of all")));
	}
	
	public static void main(String[] args) {
		getThrowableHierarchy().printStackTrace();
	}
}
