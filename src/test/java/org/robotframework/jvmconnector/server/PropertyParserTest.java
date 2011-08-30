package org.robotframework.jvmconnector.server;

import org.jmock.MockObjectTestCase;
import org.robotframework.jvmconnector.common.PropertyParsingFailedException;
import org.springframework.beans.PropertyValue;


public class PropertyParserTest extends MockObjectTestCase {
	private String propertyString = "property1=value1|property2=value2";
	private PropertyParser propertyParser;
	private String[] propertiesAsString;

	public void setUp() {
		propertyParser = new PropertyParser(propertyString);
		propertiesAsString = propertyString.split("\\|");
	}

	public void testParsesPropertiesIntoAnPropertyArrayOfCorrectLength() {
		assertTrue(propertiesAsString.length == propertyParser.getPropertyValues().length);
	}

	public void testReturnsArrayOfPropertyValuesFromGivenPropertyString() {
		assertPropertyValuesMatchWithThePropertyString(propertyString, propertyParser.getPropertyValues());
	}

	public void testThrowsPropertyParsingFailedWithBadPropertyPatterns() {
		PropertyParser parserWithBadPattern = new PropertyParser("bad pattern");
		try {
			parserWithBadPattern.getPropertyValues();
		} catch (PropertyParsingFailedException e) {
			return;
		}
		fail("Excpected PropertyParsingFailedException to be thrown");
	}

	private void assertPropertyValuesMatchWithThePropertyString(String propertyString, PropertyValue[] propertyValues) {
		final int NAME = 0;
		final int VALUE = 1;

		for (int i = 0; i < propertyValues.length; ++i) {
			String[] propertyAsString = propertiesAsString[i].split("=");

			assertEquals(propertyAsString[NAME], propertyValues[i].getName());
			assertEquals(propertyAsString[VALUE], propertyValues[i].getValue());
		}
	}
}
