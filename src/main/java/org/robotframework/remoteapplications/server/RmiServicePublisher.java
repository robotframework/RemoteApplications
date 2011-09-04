/*
 * Copyright 2008-2011 Nokia Siemens Networks Oyj
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


package org.robotframework.remoteapplications.server;

import java.rmi.RemoteException;

import org.springframework.remoting.rmi.RmiServiceExporter;

public class RmiServicePublisher {
    private final RmiServiceExporter serviceExporter;

    public RmiServicePublisher() {
        this(new RmiServiceExporter());
    }
    
    public RmiServicePublisher(RmiServiceExporter serviceExporter) {
        this.serviceExporter = serviceExporter;
    }
    
    public String publish(String serviceName, Class<?> serviceInterface, Object service, int registryPort) {
        serviceExporter.setServiceName(serviceName);
        serviceExporter.setRegistryPort(registryPort);
        serviceExporter.setService(service);
        serviceExporter.setServiceInterface(serviceInterface);
        try {
            serviceExporter.prepare();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        
        return "rmi://localhost:" + registryPort + "/" + serviceName;
    }
}
