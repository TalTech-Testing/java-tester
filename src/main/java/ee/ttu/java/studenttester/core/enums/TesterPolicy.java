package ee.ttu.java.studenttester.core.enums;

import ee.ttu.java.studenttester.core.interfaces.SecurityPolicy;
import ee.ttu.java.studenttester.core.security.ExtReflectPermission;
import ee.ttu.java.studenttester.core.security.PermissionContext;
import ee.ttu.java.studenttester.core.security.SecureEnvironment;

import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.SocketPermission;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static ee.ttu.java.studenttester.core.helpers.ClassUtils.getExtReflectPermission;
import static ee.ttu.java.studenttester.core.helpers.ClassUtils.getTopLevelClass;

public enum TesterPolicy implements SecurityPolicy {

    /**
     * Disable System.exit().
     */
    DISABLE_EXIT(TesterPolicy::disableExit),

    /**
     * Disable <<ALL FILES>>-type file access.
     */
    DISABLE_ANY_FILE_MATCHER(TesterPolicy::disableAnyFileMatcher),

    /**
     * Disable System.setSecurityManager().
     */
    DISABLE_SECURITYMANAGER_CHANGE(TesterPolicy::disableSecurityManagerChange),

    /**
     * Entrirely disable reflection. Breaks many things such as sockets.
     */
    DISABLE_REFLECTION_STRICT(TesterPolicy::disableReflectionStrict),

    /**
     * Disable reflection invoked directly from a blacklisted class.
     */
    DISABLE_REFLECTION_UNTRUSTED(TesterPolicy::disableReflectionUntrustedShallow),

    /**
     * Disable reflection invoked directly from a blacklisted class targeted at a protected class.
     */
    DISABLE_REFLECTION_SELECTIVE(TesterPolicy::disableReflectionSelective),

    /**
     * Disable invocation of external commands.
     */
    DISABLE_EXECUTION(TesterPolicy::disableExec),

    /**
     * Disable read/write access to test files.
     */
    DISABLE_TEST_SNIFFING(TesterPolicy::disableTestSniffing),

    /**
     * Disable Internet access.
     */
    DISABLE_SOCKETS(TesterPolicy::disableSockets);

    public static final String ALL_FILES = "<<ALL FILES>>";
    private static final SecureEnvironment secInstance = SecureEnvironment.getInstance();
    private final Consumer<PermissionContext> permissionConsumer;

    TesterPolicy(Consumer<PermissionContext> permissionConsumer) {
        this.permissionConsumer = permissionConsumer;
    }

    public Consumer<PermissionContext> getConsumer() {
        return this.permissionConsumer;
    }

    /**
     * Checks if the permission is about exiting the VM.
     * @param pc - permission context to check
     */
    private static void disableExit(PermissionContext pc) {
        if (pc.permission.getName() != null && pc.permission.getName().contains("exitVM")) {
            throw new SecurityException("Illegal attempt to exit the JVM.");
        }
    }

    /**
     * Checks if the permission is about accessing any file. Good for disabling commands without an explicit path but not
     * much else.
     * @param pc - permission context to check
     */
    private static void disableAnyFileMatcher(PermissionContext pc) {
        if (!(pc.permission instanceof FilePermission)) {
            return;
        }
        if (pc.permission.getName().contains(ALL_FILES)) {
            throw new SecurityException("Illegal attempt to access the file system.");
        }
    }

    /**
     * Checks if the permission is about changing the security manager.
     * @param pc - permission context to check
     */
    private static void disableSecurityManagerChange(PermissionContext pc) {
        if (pc.permission.getName().contains("setSecurityManager")) {
            throw new SecurityException("Illegal attempt to modify the security manager.");
        }
    }

    /**
     * Checks if the permission is about reflection.
     * @param pc - permission context to check
     */
    private static void disableReflectionStrict(PermissionContext pc) {
        if (pc.permission instanceof ReflectPermission) {
            throw new SecurityException("Illegal attempt to use reflection.");
        }
    }

    /**
     * Checks if the permission is about reflection and is invoked directly by a blacklisted class.
     * @param pc - permission context to check
     */
    private static void disableReflectionUntrustedShallow(PermissionContext pc) {
        if (pc.permission instanceof ReflectPermission) {
            var optionalClass = pc.executionStack.stream()
                    .skip(1)
                    .filter(c -> !c.getPackage().getName().startsWith("java.lang.reflect"))
                    .findFirst();
            optionalClass.ifPresent(caller -> {
                // get the top level class to find the source
                caller = getTopLevelClass(caller);
                if (secInstance.getBlackListedClasses().contains(caller)) {
                    throw new SecurityException("Illegal attempt to use reflection.");
                }
            });
        }
    }

    /**
     * Checks if the permission is about reflection and is invoked directly by a blacklisted class.
     * @param pc - permission context to check
     */
    private static void disableReflectionSelective(PermissionContext pc) {
        ExtReflectPermission ep = getExtReflectPermission(pc.permission);
        if (ep != null) {
            AccessibleObject ao = ep.accessibleObject;
            if (ao instanceof Member) {
                Class declaringClass = getTopLevelClass(((Member) ao).getDeclaringClass());
                if (pc.secureEnvironment.getProtectedClasses().contains(declaringClass)) {
                    throw new SecurityException("Illegal attempt to access a class: " + declaringClass);
                }
            }
        }
    }

    /**
     * Checks if the permission has "execute" flag set.
     * @param pc - permission context to check
     */
    private static void disableExec(PermissionContext pc) {
        if (pc.permission.getActions().contains("execute")) {
            throw new SecurityException(String.format("Illegal attempt to execute a resource: %s", pc.permission.getName()));
        }
    }

    /**
     * Checks if the permission attempts to access any protected file.
     * @param pc - permission context to check
     */
    private static void disableTestSniffing(PermissionContext pc) {
        if (!(pc.permission instanceof FilePermission)) {
            return;
        }
        for (Path path : secInstance.getProtectedFiles()) {
            try {
                if (pc.permission.getName().equals(ALL_FILES) || Files.isSameFile(path, Paths.get(pc.permission.getName()))) {
                    throw new SecurityException(String.format("Illegal attempt to access resource: %s", pc.permission.getName()));
                }
            } catch (IOException e) {
                // ignore, the file probably does not exist or is not accessible anyway.
            }
        }
    }

    /**
     * Checks if the permission is about network sockets.
     * @param pc - permission context to check
     */
    private static void disableSockets(PermissionContext pc) {
        if (pc.permission instanceof SocketPermission) {
            throw new SecurityException(String.format("Illegal attempt to open a network socket: %s", pc.permission.getName()));
        }
    }
}
