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

public class RmiService {
    private final Class<LibraryImporter> serviceInterface = LibraryImporter.class;
    private RmiServicePublisher rmiPublisher = new RmiServicePublisher();

    public void start(final String pathToRmiStorage) {
        String rmiInfo = start(new FreePortFinder().findFreePort());
        new RmiInfoStorage(pathToRmiStorage).store(rmiInfo);
    }

    public String start(int rmiPort) {
        RemoteLibraryImporter libraryImporter = new RemoteLibraryImporter(rmiPort, rmiPublisher);
        return rmiPublisher.publish("robotrmiservice", serviceInterface, libraryImporter, rmiPort);
    }
}