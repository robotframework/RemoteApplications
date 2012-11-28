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

import java.lang.instrument.Instrumentation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassPathAppenderFactory {
    public ClassPathAppender create(Instrumentation inst) {
        if (isJava6orLater())
            return new Java6ClassPathAppender(inst);
        else
            return new NullClassPathAppender();
    }

    private static boolean isJava6orLater() {
        String version = System.getProperty("java.version");
        Matcher m = Pattern.compile("(\\d+)\\.(\\d+)\\.(.+)").matcher(version);
        if (!m.find())
            return false;
        Integer major = Integer.parseInt(m.group(1));
        Integer minor = Integer.parseInt(m.group(2));
        return (major >= 1) && (minor >= 6);
    }
}
