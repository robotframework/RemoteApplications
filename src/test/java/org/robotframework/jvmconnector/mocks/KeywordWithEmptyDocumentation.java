package org.robotframework.jvmconnector.mocks;

import org.robotframework.javalib.keyword.DocumentedKeyword;
import org.robotframework.javalib.keyword.Keyword;

public class KeywordWithEmptyDocumentation implements DocumentedKeyword {
    private final Keyword keyword;

    public KeywordWithEmptyDocumentation(Keyword keyword) {
        this.keyword = keyword;
    }
    
    public Object execute(Object[] arguments) {
        return keyword.execute(arguments);
    }
    
    public String[] getArgumentNames() {
        return null;
    }

    public String getDocumentation() {
        return null;
    }
}
