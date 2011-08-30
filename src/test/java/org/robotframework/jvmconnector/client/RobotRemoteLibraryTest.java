package org.robotframework.jvmconnector.client;

import java.util.Arrays;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.jvmconnector.common.PropertyOverrider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;


public class RobotRemoteLibraryTest extends MockObjectTestCase {
	private Mock mockBeanFactory = mock(ConfigurableListableBeanFactory.class);
	private Mock mockPropertyOverrider = mock(PropertyOverrider.class);
	private Mock mockRmiClient = mock(RobotJavaLibrary.class);

	private ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) mockBeanFactory.proxy();
	private PropertyOverrider propertyOverrider = (PropertyOverrider) mockPropertyOverrider.proxy();
	
	private MockRemoteLibrary remoteLibraryWithMockRmiClient = new MockRemoteLibrary() {
        RobotJavaLibrary createRobotLibraryClient(String uri) {
			return (RobotJavaLibrary) mockRmiClient.proxy();
		}
	};

	protected void setUp() throws Exception {
		mockBeanFactory.stubs();
	}

	public void testOverridesDefaultValues() throws Exception {
		String uri = "rmi://somehost:9999/someLibrary";
		mockPropertyOverrider.expects(once()).method("addOverridableProperty")
		    .with(eq("robotRmiService.serviceUrl"),	eq(uri));
		mockPropertyOverrider.expects(once()).method("postProcessBeanFactory")
		    .with(same(beanFactory));

		new MockRemoteLibrary(uri);
	}

	public void testDelegatesRunKeywordCallToRmiClient() throws Exception {
		String keywordName = "testKeyword";
		Object[] keywordArgs = new Object[] { "keywordArg" };
		Object expectedReturnValue = "keywordReturnValue";

		mockRmiClient.expects(once()).method("runKeyword").with(eq(keywordName), eq(keywordArgs)).will(
			returnValue(expectedReturnValue));

		assertEquals(expectedReturnValue, remoteLibraryWithMockRmiClient.runKeyword(keywordName, keywordArgs));
	}

	public void testReturnsKeywordNames() throws Exception {
	    String clientKeyword = "someKeyword";

	    mockRmiClient.expects(once()).method("getKeywordNames")
		    .will(returnValue(new String[] { clientKeyword }));
		
        String[] expectedKeywordNames = new String[] { clientKeyword };
		
		assertKeywordNamesEquals(expectedKeywordNames, remoteLibraryWithMockRmiClient.getKeywordNames());
	}
	
	private void assertKeywordNamesEquals(String[] expectedKeywordNames, String[] keywordNames) {
	    assertEquals(Arrays.asList(expectedKeywordNames), Arrays.asList(keywordNames));
	}

    private class MockRemoteLibrary extends RobotRemoteLibrary {
        public MockRemoteLibrary(String uri) {
            super(uri);
        }
        
		public MockRemoteLibrary() {}

		ConfigurableListableBeanFactory createBeanFactory() {
			return beanFactory;
		}

		PropertyOverrider createPropertyOverrider() {
			return propertyOverrider;
		}
	}
}
