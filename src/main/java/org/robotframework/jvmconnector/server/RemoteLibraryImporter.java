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

import org.robotframework.javalib.library.RobotJavaLibrary;

public class RemoteLibraryImporter implements LibraryImporter {
    private final int rmiPort;
    private final RmiServicePublisher rmiPublisher;
    
    public RemoteLibraryImporter(int rmiPort, RmiServicePublisher rmiPublisher) {
        this.rmiPort = rmiPort;
        this.rmiPublisher = rmiPublisher;
    }

    public void closeService() {
        System.exit(0);
    }

    public String importLibrary(String libraryName) {
        String serviceName = libraryName.replace('.', '_');
        SimpleRobotRmiService rmiService = new SimpleRobotRmiService();
        rmiService.setLibrary(instantiateLibrary(libraryName));
        RobotRmiService service = new CloseableRobotRmiService(rmiService);
        
        return rmiPublisher.publish(serviceName, RobotRmiService.class, service, rmiPort);
    }

    public boolean ping() {
        return true;
    }

    private RobotJavaLibrary instantiateLibrary(String libraryName) {
        try {
            return (RobotJavaLibrary) Class.forName(libraryName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}