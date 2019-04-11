package ee.ttu.java.studenttester.core.policy;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple interface for testing.
 */
public class MockRevisedAccessibleObject {

    // direct assignment will NOT work when this class is loaded as an interceptor
    public static List<AccessibleObject> forbiddenObjects = List.of();

    public static void setAccessible(@This AccessibleObject ao, @Argument(0) boolean flag) {
        if (ao == null) {
            throw new NullPointerException("Reference to the original object is null");
        }
        if (forbiddenObjects.contains(ao)) {
            throw new ArithmeticException("Actually wanted");
        }
    }

    public static void setAccessible(AccessibleObject[] array, boolean flag) {
        if (!Collections.disjoint(Arrays.asList(array), forbiddenObjects)) {
            throw new ArithmeticException("Actually wanted");
        }
    }

}