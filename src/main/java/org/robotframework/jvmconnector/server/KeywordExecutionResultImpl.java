/*
 * Copyright 2008 Nokia Siemens Networks Oyj
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.jvmconnector.server;

import org.robotframework.javalib.util.StdStreamRedirecter;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;
import org.robotframework.jvmconnector.common.TestFailedException;

/**
 * Bean that wraps keyword execution results, logging output and exceptions it
 * throws.
 */
public class KeywordExecutionResultImpl implements KeywordExecutionResult {
    private static final long serialVersionUID = 2789824561150394856L;

    private Object keywordExecutionResult;
    private String stdOutAsString;
    private String stdErrAsString;
    private TestFailedException testFailedException;
    private boolean keywordPassed = true;

    public Object getResult() {
        return keywordExecutionResult;
    }

    public boolean keywordPassed() {
        return keywordPassed;
    }

    public String getStdErrAsString() {
        return stdErrAsString;
    }

    public String getStdOutAsString() {
        return stdOutAsString;
    }

    public TestFailedException getTestFailedException() {
        return testFailedException;
    }

    public void setResult(Object keywordExecutionResult) {
        this.keywordExecutionResult = keywordExecutionResult;
    }

    public void setTestFailedException(TestFailedException testFailedException) {
        this.testFailedException = testFailedException;
        keywordPassed = false;
    }

    /**
     * @param streamRedirecter StdStreamRedirecter providing STDOUT and STDERR as
     * string
     */
    public void setStdStreams(StdStreamRedirecter streamRedirecter) {
        stdOutAsString = streamRedirecter.getStdOutAsString();
        stdErrAsString = streamRedirecter.getStdErrAsString();
    }
}
