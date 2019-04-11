package ee.ttu.java.studenttester.core.policy;

import ee.ttu.java.studenttester.core.security.InterceptorBuilder;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static ee.ttu.java.studenttester.core.security.InterceptorBuilder.SET_ACCESSIBLE;
import static ee.ttu.java.studenttester.core.security.InterceptorBuilder.SET_ACCESSIBLE0;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class MethodInterceptionTest {

    @Test
    @Ignore
    private void test() throws Exception {
        InterceptorBuilder.installAgent();

        Method setAccessible0 = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE0, boolean.class);
        Method setAccessibleArray = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE, AccessibleObject[].class, boolean.class);

        Method constrSetAccessible = Constructor.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
        Method fieldSetAccessible = Field.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
        Method methodSetAccessible = Method.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
        Method accObjSetAccessible = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);

        var methods = List.of(constrSetAccessible, fieldSetAccessible, methodSetAccessible, accObjSetAccessible);

        Method testMethod = TestTarget.class.getDeclaredMethod("getValue");
        Constructor testConstructor = TestTarget.class.getDeclaredConstructor();
        Field testField = TestTarget.class.getDeclaredField("value");

        List<AccessibleObject> accObjs = List.of(testMethod, testConstructor, testField);

        accObjs.forEach(obj -> obj.setAccessible(true));

        TestTarget tt = (TestTarget) testConstructor.newInstance();
        testField.set(tt, 42);
        assertEquals(testMethod.invoke(tt), 42);

        accObjs.forEach(obj -> obj.setAccessible(false));

        InterceptorBuilder.transformMethod(MockRevisedAccessibleObject.class, true, setAccessibleArray, null);
        methods.forEach(method ->
                InterceptorBuilder.transformMethod(MockRevisedAccessibleObject.class, false,
                        method, setAccessible0, 0));

        // use instead of direct assignment
        ClassLoader.getPlatformClassLoader().loadClass(MockRevisedAccessibleObject.class.getName())
                .getDeclaredField("forbiddenObjects").set(null, accObjs);

        accObjs.forEach(obj -> assertThrows(ArithmeticException.class, () -> obj.setAccessible(true)));
        assertThrows(IllegalAccessException.class, () -> testMethod.invoke(tt));
        assertThrows(IllegalAccessException.class, testConstructor::newInstance);
        assertThrows(IllegalAccessException.class, () -> testField.set(tt, 42));

        assertThrows(ArithmeticException.class,
                () -> AccessibleObject.setAccessible(new AccessibleObject[] { testConstructor }, true));

    }

}


