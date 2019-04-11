package ee.ttu.java.studenttester.core.security;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.AccessibleObject;

public class RevisedAccessibleObject {

    public static void setAccessible(@This AccessibleObject ao, @Argument(0) boolean flag) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(new ExtReflectPermission(ao, flag));
        }
    }

    public static void setAccessible(AccessibleObject[] array, boolean flag) {
        SecurityManager sec = System.getSecurityManager();
        for (AccessibleObject ao : array) {
            if (sec != null) {
                sec.checkPermission(new ExtReflectPermission(ao, flag));
            }
        }
    }

    private RevisedAccessibleObject() {
        // no instances
    }
}
