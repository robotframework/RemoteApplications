package org.robotframework.jvmconnector.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LoggerUtil {
	private static final PrintStream ORIGINAL_SYSTEM_OUT = System.out;
	private static final PrintStream ORIGINAL_SYSTEM_ERR = System.err;
	
	public static ByteArrayOutputStream redirectSystemOut() {
		ByteArrayOutputStream logStringBuffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(logStringBuffer));
		return logStringBuffer;
	}
	
	public static ByteArrayOutputStream redirectSystemErr() {
		ByteArrayOutputStream logStringBuffer = new ByteArrayOutputStream();
		System.setErr(new PrintStream(logStringBuffer));
		return logStringBuffer;
	}
	
	public static void resetSystemOut() {
		System.setOut(ORIGINAL_SYSTEM_OUT);
	}
	
	public static void resetSystemErr() {
		System.setErr(ORIGINAL_SYSTEM_ERR);
	}
}
