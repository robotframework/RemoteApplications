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
