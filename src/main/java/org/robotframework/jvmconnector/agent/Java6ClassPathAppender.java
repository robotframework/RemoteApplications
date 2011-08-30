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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

public class Java6ClassPathAppender implements ClassPathAppender {
    private final Instrumentation instrumentation;

    public Java6ClassPathAppender(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void appendToClasspath(JarFile jar) {        
        try {
            appendWithReflection(jar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void appendWithReflection(JarFile jar) throws IllegalAccessException, InvocationTargetException {
        appendMethod().invoke(instrumentation, jar);
    }

    private Method appendMethod() {
        try {
            return instrumentation.getClass()
                                  .getMethod("appendToSystemClassLoaderSearch", JarFile.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
