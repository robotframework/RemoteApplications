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


package org.robotframework.remoteapplications.agent;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class AgentConfiguration {

    private Integer port;
    private List<String> jars = new ArrayList<String>();
    private String separator;

    public AgentConfiguration(String arguments) {

        List<String> splittedArguments = Arrays.asList(arguments.split(File.pathSeparator));
        parsePort(splittedArguments);
        parseJars(splittedArguments);
    }

    private void parsePort(List<String> arguments) {
        for (String item : arguments) {
            if (isPort(item)) {
                port = Integer.valueOf(item.substring(5));
                return;
            }
        }
    }

    private boolean isPort(String item) {
        return item.toLowerCase().startsWith("port");
    }

    private void parseJars(List<String> arguments) {
        for (String item : arguments)
            if (!isPort(item))
                jars.add(item);
    }

    public Integer getPort() {
        return port;
    }

    public List<String> getJars() {
        return jars;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[port=" + port + ", jars=" + jars.toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jars == null) ? 0 : jars.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentConfiguration other = (AgentConfiguration) obj;
        if (jars == null) {
            if (other.jars != null)
                return false;
        } else if (!jars.equals(other.jars))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        return true;
    }
}
