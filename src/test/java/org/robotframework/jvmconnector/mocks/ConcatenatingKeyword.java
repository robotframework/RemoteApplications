package org.robotframework.jvmconnector.mocks;

import org.robotframework.javalib.keyword.DocumentedKeyword;

public class ConcatenatingKeyword implements DocumentedKeyword {
    public Object execute(Object[] arguments) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : arguments) {
            sb.append(arg);
        }
        return sb.toString();
    }

    public String[] getArgumentNames() {
        return new String[] { "arg1", "arg2", "*rest" };
    }

    public String getDocumentation() {
        return "Concatenates two or more arguments together";
    }
}
