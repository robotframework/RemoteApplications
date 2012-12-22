package org.robotframework.remoteapplications.agent;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.io.File;

import org.junit.Test;


public class AgentConfigurationTest{
    @Test
    public void parsePortFromBeginning() {
        testParser("Port=1234"+File.pathSeparator+"foo.jar", 1234, "foo.jar");
    }

    @Test
    public void parsePortFromEnd() {
        testParser("foo.jar"+File.pathSeparator+"port=1234", 1234, "foo.jar");
    }

    @Test
    public void parsePortFromMiddle() {
        testParser("zip.jar"+File.pathSeparator+"PORT=1234"+File.pathSeparator+"foo.jar", 1234, "zip.jar"+File.pathSeparator+"foo.jar");
    }

    @Test
    public void parseWithoutPort() {
        testParser("zip.jar;foo.jar", null, "zip.jar;foo.jar");
    }

    @Test
    public void parseWithWindowsDriveLetter() {
        testParser("C:\\zip.jar;D:\\foo.jar", null, "C:\\zip.jar;D:\\foo.jar", ";");
    }

    @Test
    public void parseWithWindowsDriveLetterComplex() {
        testParser("C:\\foo\\bar\\zip.jar;foo.jar;E:\\some\\package\\foo.jar", null,
                   "C:\\foo\\bar\\zip.jar;foo.jar;E:\\some\\package\\foo.jar", ";");
    }

    @Test(expected=NumberFormatException.class)
    public void parseWithInvalidPort() {
        testParser("zip.jar"+File.pathSeparator+"port=abcd"+File.pathSeparator+"foo.jar", null, "zip.jar"+File.pathSeparator+"foo.jar");
    }

    private void testParser(String input, Integer port, String jars, String splitter) {
        AgentConfiguration conf = new AgentConfiguration(input, splitter);
        assertEquals(port, conf.getPort());
        List<String> expected = Arrays.asList(jars.split(splitter));
        assertEquals(expected, conf.getJars());
    }

    private void testParser(String input, Integer port, String jars) {
        testParser(input, port, jars, File.pathSeparator);
    }
}