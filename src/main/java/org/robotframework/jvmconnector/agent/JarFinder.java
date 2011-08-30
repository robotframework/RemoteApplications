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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.io.DirectoryWalker;

public class JarFinder extends DirectoryWalker {
    private final File path;

    public JarFinder(String path) {
        this.path = new File(path);
    }

    public void each(JarFileAction fileAction) {
        List<File> jars = findJars();
        for (File jarFile : jars) {
            fileAction.doOnFile(createJar(jarFile));
        }
    }

    private JarFile createJar(File jarFile) {
        try {
            return new JarFile(jarFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleFile(File file, int depth, Collection results) throws IOException {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            results.add(file);
        }
    }
    
    private List<File> findJars() {
        List<File> jars = new ArrayList<File>();
        if (path.isDirectory()) {
            walkThrough(jars);
        } else if (path.isFile()) {
            jars.add(path);
        }
        return jars;
    }

    private void walkThrough(List<File> jars) {
        try {
            walk(path, jars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
