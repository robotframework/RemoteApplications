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

package org.robotframework.jvmconnector.common;

import java.io.File;

public class DataBasePaths {
    private boolean create;

    public DataBasePaths() {
        this(false);
    }

    public DataBasePaths(boolean createMissingDirectories) {
        this.create = createMissingDirectories;
    }

    public String getLaunchedFile() {
        return getPathToFile("launched.txt");
    }

    public String getConnectedFile() {
        return getPathToFile("connected.txt");
    }

    private String getPathToFile(String file_name) {
        String robot = getRobotDir();
        createDir(robot);
        String jvmconnector = join(robot, "jvmconnector");
        createDir(jvmconnector);
        return join(jvmconnector, file_name);
    }

	private String getRobotDir() {
		if (System.getProperty("os.name").startsWith("Windows"))
            return join(System.getenv("APPDATA"), "RobotFramework");
		return join(System.getenv("HOME"), ".robotframework");
	}

	private String join(String item1, String item2) {
		return item1 + File.separator + item2;
	}
	
	private void createDir(String path) {
        File dir = new File(path);
		if (!dir.exists() && create) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Could not create directory " + path);
            }
        }
	}

}
