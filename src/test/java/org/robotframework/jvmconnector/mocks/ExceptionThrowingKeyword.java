package org.robotframework.jvmconnector.mocks;


public class ExceptionThrowingKeyword extends LoggingKeyword {
	public static final String KEYWORD_NAME = "ExceptionThrowingKeyword";

	public Object execute(Object[] args) {
		super.execute(args);
		throw new MockException(); 
	}
	
	@Override
	public String getDocumentation() {
	    return "Throws mock exception.";
	}
}
