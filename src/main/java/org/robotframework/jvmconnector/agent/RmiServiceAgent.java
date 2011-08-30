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

package org.robotframework.jvmconnector.agent;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.jar.JarFile;

import org.robotframework.jvmconnector.common.DataBasePaths;
import org.robotframework.jvmconnector.server.RmiService;

public class RmiServiceAgent {
    private static String launched = new DataBasePaths(true).getLaunchedFile();
    private static ClassPathAppenderFactory appenderFactory = new ClassPathAppenderFactory();

    public static void premain(String agentArguments, Instrumentation inst) {
        AgentConfiguration conf = new AgentConfiguration(agentArguments);
        setClasspath(conf.getJars(), inst);
        startRmiService(conf.getPort());
    }

    static void setClasspath(List<String> jars, final Instrumentation inst) {
        for (String file : jars) {
            addJarsToClasspath(inst, file);
        }
    }

    private static void addJarsToClasspath(final Instrumentation inst, String file) {
        new JarFinder(file).each(new JarFileAction() {
            public void doOnFile(JarFile file) {
                addToClassPath(inst, file);
            }
        });
    }

    private static void addToClassPath(Instrumentation inst, JarFile file) {
        classPathAppender(inst).appendToClasspath(file);
    }

    private static ClassPathAppender classPathAppender(Instrumentation inst) {
        return appenderFactory.create(inst);
    }

    private static void startRmiService(Integer port) {
        if (port != null)
            new RmiService().start(port);
        else
            new RmiService().start(launched);
    }
}
