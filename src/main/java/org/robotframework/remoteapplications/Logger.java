package org.robotframework.remoteapplications;

import java.io.PrintStream;
import java.util.Calendar;

public class Logger {

    private static PrintStream out = System.out;

    public static void log(Object message) {
        out.println("[" + callPoint() + "] [" + getTimestamp() + "] " + message.toString());
    }

    private static String getTimestamp() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
    }

    private static StackTraceElement callPoint() {
        return Thread.currentThread().getStackTrace()[3];
    }
}
