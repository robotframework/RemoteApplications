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

package org.robotframework.jvmconnector.client;

import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;
import org.robotframework.jvmconnector.server.RobotRmiService;
import org.springframework.beans.factory.BeanFactory;


/**
 * RMI client handling remote keyword invocation, the actual execution of the
 * keywords is delegated to RobotRmiService. The RMI service is exposed
 * through org.springframework.remoting.rmi.RmiProxyFactoryBean which this class
 * expects to be contained in org.springframework.beans.factory.BeanFactory that
 * it receives as a constructor parameter.
 * 
 * @see RobotRmiService
 * @see RobotRemoteLibrary
 */
public class RobotRmiClient implements RobotJavaLibrary {
    private RobotRmiService service;

    public RobotRmiClient(BeanFactory beanFactory) {
        setService(beanFactory);
    }

    public String[] getKeywordNames() {
        return service.getKeywordNames();
    }

    public Object runKeyword(String keywordName, Object[] args) {
        KeywordExecutionResult keywordExecutionResults = service.runKeyword(keywordName, args);
        printStdOut(keywordExecutionResults.getStdOutAsString());
        printStdErr(keywordExecutionResults.getStdErrAsString());

        if (!keywordExecutionResults.keywordPassed())
            throw keywordExecutionResults.getTestFailedException();

        return keywordExecutionResults.getResult();
    }

    protected void printStdOut(String stdOutAsString) {
        System.out.print(stdOutAsString);
    }

    protected void printStdErr(String stdErrAsString) {
        System.err.print(stdErrAsString);
    }

    private void setService(BeanFactory beanFactory) {
        service = (RobotRmiService) beanFactory.getBean("robotRmiService");
    }

    public boolean ping() {
        return service.ping();
    }
}
