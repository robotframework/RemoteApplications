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

package org.robotframework.jvmconnector.launch;

import static org.robotframework.javalib.util.Logger.log;

import java.util.Arrays;

import org.robotframework.javalib.util.ArrayUtil;
import org.robotframework.jvmconnector.server.ApplicationLauncher;
import org.robotframework.jvmconnector.server.RmiService;

public class RMILauncher {
    private static RmiService rmiService = new RmiService();
    private static ApplicationLauncher applicationLauncher = new ApplicationLauncher();
    
    public static void main(String[] args) throws Exception {
        log("RMILauncher: " + Arrays.asList(args));
        if (args.length < 2)
            throw new IllegalArgumentException("Usage: java RmiServiceLibrary [jvmArgs] rmiConfigFilePath applicationClassName [applicationArgs]");

        String pathToRmiStorage = args[0];
        rmiService.start(pathToRmiStorage);
        String[] restOfTheArgs = extractRestOfTheArgs(args);
        log("starting the application '" + args[1] + " with args '" + Arrays.asList(restOfTheArgs) + "'");
        
        applicationLauncher.launchApplication(args[1], restOfTheArgs);
    }

    private static String[] extractRestOfTheArgs(String[] args) {
        return ArrayUtil.<String>copyOfRange(args, 2, args.length);
    }
}
