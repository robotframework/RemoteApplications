package org.robotframework.jvmconnector.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.robotframework.javalib.library.RobotJavaLibrary;

public class CloseableLibraryDecoratorTest {
    @Test
    public void getKeywordNames() {
        CloseableLibraryDecorator libDecor = new CloseableLibraryDecorator(new RobotJavaLibrary() {
            public Object runKeyword(String keywordName, Object[] args) {return null;}            
            public String[] getKeywordNames() {return new String[0];}
        } );
        String[] keywordNames = libDecor.getKeywordNames();
        assertEquals("Keyword: "+CloseableLibraryDecorator.KEYWORD_CLOSE_APPLICATION+" not found!", 
                     CloseableLibraryDecorator.KEYWORD_CLOSE_APPLICATION, 
                     keywordNames[0]);
        assertTrue("There should be only 1 keyword!", keywordNames.length == 1);
    }
}
