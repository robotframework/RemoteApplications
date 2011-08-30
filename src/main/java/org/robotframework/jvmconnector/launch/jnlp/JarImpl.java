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

package org.robotframework.jvmconnector.launch.jnlp;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class JarImpl implements Jar {
    private JarFile jarFile;
    
    public JarImpl(File file) {
        this(createJarFile(file));
    }

    JarImpl(JarFile jarFile) {
        this.jarFile = jarFile;
    }
    
    public String getPath() {
        return jarFile.getName();
    }

    public String getMainClass() {
        try {
            return jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static JarFile createJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
