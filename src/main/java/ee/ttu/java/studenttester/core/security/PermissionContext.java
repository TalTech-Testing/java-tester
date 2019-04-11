package ee.ttu.java.studenttester.core.security;

import java.security.Permission;
import java.util.List;

public class PermissionContext {

    public final List<Class> executionStack;
    public final Permission permission;
    public final SecureEnvironment secureEnvironment;

    public PermissionContext(List<Class> executionStack, Permission permission, SecureEnvironment secureEnvironment) {
        this.executionStack = executionStack;
        this.permission = permission;
        this.secureEnvironment = secureEnvironment;
    }

}
