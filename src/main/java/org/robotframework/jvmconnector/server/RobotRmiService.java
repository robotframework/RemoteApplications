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

package org.robotframework.jvmconnector.server;

import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;


/**
 * The service interface of the jvmconnector module. Acts as a connecting point
 * between the org.robotframework.javalib.library.RobotJavaLibrary and
 * RobotJavaLibrary.
 */
public interface RobotRmiService {
	/**
	 * @param library
	 *            org.robotframework.javalib.library.RobotJavaLibrary
	 *            this service delegates to.
	 */
	void setLibrary(RobotJavaLibrary library);

	/**
	 * @return the keyword names from the contained
	 *         org.robotframework.javalib.library.RobotJavaLibrary
	 */
	String[] getKeywordNames();

	/**
	 * @see KeywordExecutionResult
	 */
	KeywordExecutionResult runKeyword(String keywordName, Object[] keywordArguments);
	
	/**
	 * Used to see if the connection is alive.
	 */
	boolean ping();
}
