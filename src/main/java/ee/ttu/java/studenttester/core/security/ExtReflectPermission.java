package ee.ttu.java.studenttester.core.security;

import java.lang.reflect.AccessibleObject;
import java.security.BasicPermission;

/**
 * Extended reflection permission that retains information about its target object.
 */
public class ExtReflectPermission extends BasicPermission {

    public final AccessibleObject accessibleObject;
    public final boolean flag;

    public ExtReflectPermission(AccessibleObject accessibleObject, boolean flag) {
        super(accessibleObject.toString() + ": " + flag);
        this.accessibleObject = accessibleObject;
        this.flag = flag;
    }
    
}