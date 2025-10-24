package com.nonameradio.app.core.di;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceLocator
 */
public class ServiceLocatorTest {

    @Mock
    private TestService mockService;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void testServiceRegistrationAndResolution() {
        // Register mock service
        ServiceLocator.register(TestService.class, () -> mockService);

        // Resolve service
        TestService resolved = ServiceLocator.get(TestService.class);

        // Verify
        assertNotNull(resolved);
        assertEquals(mockService, resolved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnregisteredServiceThrowsException() {
        // Try to get unregistered service
        ServiceLocator.get(UnregisteredService.class);
    }

    @Test
    public void testSingletonInstance() {
        ServiceLocator instance1 = ServiceLocator.getInstance();
        ServiceLocator instance2 = ServiceLocator.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertEquals(instance1, instance2);
    }

    // Test interfaces and classes
    public interface TestService {
        void doSomething();
    }

    public interface UnregisteredService {
        void doSomethingElse();
    }
}
