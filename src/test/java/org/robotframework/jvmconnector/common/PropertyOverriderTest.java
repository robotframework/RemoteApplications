package org.robotframework.jvmconnector.common;

import java.util.Properties;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class PropertyOverriderTest extends MockObjectTestCase {
    private Mock mockInnerProps;
    private PropertyOverrider propertyOverrider;

    protected void setUp() throws Exception {
        mockInnerProps = mock(Properties.class);
        propertyOverrider = new PropertyOverrider((Properties) mockInnerProps.proxy());
    }

    public void testOverridingPropertyAddsToTheProperties() throws Exception {
        String testValue = "testValue";
        String testName = "testName";
        mockInnerProps.expects(once()).method("setProperty").with(eq(testName), eq(testValue));

        propertyOverrider.addOverridableProperty(testName, testValue);
    }

    public void testUsesInnerPropsForPostProcessingBeanFactory() throws Exception {
        MockPropertyOverrider overrider = new MockPropertyOverrider((Properties) mockInnerProps.proxy());
        overrider.postProcessBeanFactory(((ConfigurableListableBeanFactory) mock(ConfigurableListableBeanFactory.class)
            .proxy()));
        assertEquals(1, overrider.counter);
    }
    
    public void testCreatesDefaultPropertiesIfNotProvided() {
        new PropertyOverrider().addOverridableProperty("testName", "testValue");
    }
}

class MockPropertyOverrider extends PropertyOverrider {
    int counter = 0;
    private Properties properties;

    public MockPropertyOverrider(Properties properties) {
        super(properties);
        this.properties = properties;
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFact, Properties props) throws BeansException {
        if (props == properties)
            ++counter;
    }
}
