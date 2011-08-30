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

import java.util.Arrays;

import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.javalib.util.Logger;
import org.robotframework.javalib.util.StdStreamRedirecter;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;
import org.robotframework.jvmconnector.common.TestFailedException;

/**
 * Robot RMI service that handles the communication between the RMI client and
 * the org.robotframework.javalib.library.RobotJavaLibrary.
 */
public class SimpleRobotRmiService implements RobotRmiService {
    private RobotJavaLibrary library;
    private final StdStreamRedirecter streamRedirecter;

    public SimpleRobotRmiService() {
        this(new StdStreamRedirecter());
    }

    public SimpleRobotRmiService(StdStreamRedirecter streamRedirecter) {
        this.streamRedirecter = streamRedirecter;
    }

    public String[] getKeywordNames() {
        String[] keywordNames = library.getKeywordNames();
        Logger.log(Arrays.asList(keywordNames));
        return keywordNames;
    }

    public void setLibrary(RobotJavaLibrary library) {
        Logger.log("setting library " + library.getClass().getName());
        this.library = library;
    }

    public KeywordExecutionResult runKeyword(String keywordName, Object[] keywordArguments) {
        streamRedirecter.redirectStdStreams();
        try {
            return executeKeyword(keywordName, keywordArguments);
        } finally {
            streamRedirecter.resetStdStreams();
        }
    }

    private KeywordExecutionResult executeKeyword(String keywordName, Object[] keywordArguments) {
        KeywordExecutionResultImpl keywordExecutionResult = new KeywordExecutionResultImpl();
        try {
            keywordExecutionResult.setResult(library.runKeyword(keywordName, keywordArguments));
        } catch (Throwable e) {
            keywordExecutionResult.setTestFailedException(new TestFailedException(e));
        }
        keywordExecutionResult.setStdStreams(streamRedirecter);
        return keywordExecutionResult;
    }
    
    public boolean ping() {
        return true;
    }
}
