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

import org.robotframework.jvmconnector.common.PropertyParsingFailedException;
import org.springframework.beans.PropertyValue;


/**
 * Parses patterns of form
 * <code>someProperty=someValue|anotherProperty=anotherValue</code> into
 * an array of org.springframework.beans.PropertyValue.
 */
public class PropertyParser {
	private static final String PROPERTIES_SEPARATOR_CHAR = "\\|";
	private static final String NAME_VALUE_SEPARATOR_CHAR = "=";
	
	private static final String PARSING_FAILED_MESSAGE = 
		"Please provide property pattern in the following form: " +
		"'someProperty" + NAME_VALUE_SEPARATOR_CHAR + "someValue|" +
		"otherProperty" +  NAME_VALUE_SEPARATOR_CHAR + "otherValue'";
	
	private String propertiesPattern;
	
	public PropertyParser(String propertiesPattern) {
		this.propertiesPattern = propertiesPattern;
	}

	public PropertyValue[] getPropertyValues() {
		PropertyValue[] properties = null;
		
		try {
			properties = createPropertyValues();
		} catch (RuntimeException e) {
			throw new PropertyParsingFailedException("Parsing of property pattern '" + 
					propertiesPattern + "' failed." + PARSING_FAILED_MESSAGE);
		}
		
		return properties;
	}

	private PropertyValue[] createPropertyValues() {
		PropertyValue[] properties = new PropertyValue[getPropertiesAsString().length];
		
		for (int i = 0; i < properties.length; ++i) 
			properties[i] = createPropertyValue(i);
		return properties;
	}
	
	private PropertyValue createPropertyValue(int index) {
		String propertyAsString = getPropertyAsString(index);
		String[] propertyAr = propertyAsString.split(NAME_VALUE_SEPARATOR_CHAR);
		return new PropertyValue(propertyAr[0], propertyAr[1]);
	}
	
	private String getPropertyAsString(int index) {
		return getPropertiesAsString()[index];
	}
	
	private String[] getPropertiesAsString() {
		return propertiesPattern.split(PROPERTIES_SEPARATOR_CHAR);
	}
}
