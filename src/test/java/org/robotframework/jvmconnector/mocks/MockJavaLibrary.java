package org.robotframework.jvmconnector.mocks;

import java.util.HashMap;
import java.util.Map;

import org.robotframework.javalib.keyword.DocumentedKeyword;
import org.robotframework.javalib.keyword.Keyword;
import org.robotframework.jvmconnector.xmlrpc.RobotLibrary;

public class MockJavaLibrary implements RobotLibrary  {
    @SuppressWarnings("serial")
    public Map<String, DocumentedKeyword> keywords = new HashMap<String, DocumentedKeyword>() {{
        put("concatenatingKeyword", new ConcatenatingKeyword());
        put(LoggingKeyword.KEYWORD_NAME, new LoggingKeyword());
        put(ExceptionThrowingKeyword.KEYWORD_NAME, new ExceptionThrowingKeyword());
    }};

    public String[] getKeywordNames() {
        return (String[]) keywords.keySet().toArray(new String[0]);
    }

    public Object runKeyword(String keywordName, Object[] args) {
        Keyword keyword = keywords.get(keywordName);
        if (keyword == null)
            throw new MockException("Failed to find keyword '" + keywordName + "'");

        return keyword.execute(args);
    }

    public String[] getKeywordArguments(String keywordName) {
        return keywords.get(keywordName)
                       .getArgumentNames();
    }

    public String getKeywordDocumentation(String keywordName) {
        return keywords.get(keywordName)
                       .getDocumentation();
    }
}
