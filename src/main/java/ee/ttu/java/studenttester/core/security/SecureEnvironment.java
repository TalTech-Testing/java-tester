package ee.ttu.java.studenttester.core.security;

import ee.ttu.java.studenttester.core.interfaces.SecurityPolicy;

import java.nio.file.Path;
import java.security.Permission;
import java.util.*;
import java.util.logging.Logger;

import static ee.ttu.java.studenttester.core.enums.TesterPolicy.*;

public class SecureEnvironment {

    private static final Logger LOG = Logger.getLogger(SecureEnvironment.class.getName());

    private static final SecureEnvironment instance = new SecureEnvironment();

    /**
     * Holds all the functions responsible for checking various permissions.
     */
    private static final Set<SecurityPolicy> policies = new HashSet<>();

    /**
     * The custom security manager.
     */
    private static final SecurityManager securityManager = new SecurityManager() {

        /**
         * Decides whether to allow an action or not based on the current active policies, blacklisted classes
         * and the current execution stack. If any of the blacklisted classes are present in the stack, it can be
         * presumed that the action originates from that class.
         * @param permission
         */
        @Override
        public void checkPermission(final Permission permission) {
            List<Class> stack = Arrays.asList(getClassContext());

            // if no blacklisted classes are in the stack (or not recursive)
            if (stack.subList(1, stack.size()).contains(getClass()) || Collections.disjoint(stack, classBlacklist)) {
                // allow everything
                return;
            }
            // if null/custom and blacklisted classes present, something tried to access this class
            if (permission == null || permission instanceof StudentTesterAccessPermission) {
                throw new SecurityException("Security check failed.");
            }
            // else iterate over all active policies and call their respective methods
            PermissionContext pc = new PermissionContext(stack, permission, instance);
            for (var policy : policies) {
                try {
                    policy.getConsumer().accept(pc);
                } catch (SecurityException e) {
                    triggered = true;
                    // Illegal attempt caught, log an error or do smth
                    LOG.severe(String.format("Illegal attempt caught: %s",  permission.toString()));
                    throw e;
                }

            }
        }
    };

    /**
     * Stores the original security manager.
     */
    private static final SecurityManager defaultSecurityManager = System.getSecurityManager();

    /**
     * Stores classes that are subject to checks.
     */
    private static final Set<Class> classBlacklist = new HashSet<>();

    /**
     * Stores file paths that are "protected" (files with these names cannot be read or written to).
     */
    private static final Set<Path> protectedFiles = new HashSet<>();

    /**
     * Stores classes that are protected from reflection.
     */
    private static final Set<Class> protectedClasses = new HashSet<>();

    /**
     * Indicates whether a security violation has taken place.
     */
    private static boolean triggered = false;

    /**
     * Restores the original security manager and clears all variables.
     */
    public void resetAll() {
        triggered = false;
        classBlacklist.clear();
        policies.clear();
        protectedFiles.clear();
        System.setSecurityManager(defaultSecurityManager);
    }

    /**
     * Sets the default restrictions.
     */
    public void setDefaultRestrictions() {
        addPolicy(DISABLE_EXIT);
        addPolicy(DISABLE_ANY_FILE_MATCHER);
        addPolicy(DISABLE_SECURITYMANAGER_CHANGE);
        //addPolicy(DISABLE_REFLECTION_UNTRUSTED);
        addPolicy(DISABLE_EXECUTION);
        addPolicy(DISABLE_TEST_SNIFFING);
        addPolicy(DISABLE_REFLECTION_SELECTIVE);
        addPolicy(DISABLE_SOCKETS);

        InterceptorBuilder.installAgent();
        InterceptorBuilder.initDefaultTransformations();
    }

    /**
     * Sets the new security manager.
     */
    public void enableCustomSecurityManager() {
        System.setSecurityManager(securityManager);
    }

    /**
     * Restores the old security manager.
     */
    public void disableCustomSecurityManager() {
        System.setSecurityManager(securityManager);
    }

    /**
     * Returns the singleton of this class if the security manager has not been changed or the caller is allowed to
     * access this instance.
     * @return the singleton of this class, if the action was allowed
     */
    public static SecureEnvironment getInstance() {
        // if the security manager has been changed, see if the caller is allowed to access it
        checkAccess();
        return instance;
    }

    public static void checkAccess() {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager()
                    .checkPermission(new StudentTesterAccessPermission("ACCESS"));
        }
    }

    // no checkAccess() needed
    public void addPolicy(SecurityPolicy policy) {
        policies.add(policy);
    }
    public void removePolicy(SecurityPolicy policy) {
        policies.remove(policy);
    }
    public Set<SecurityPolicy> getCurrentPolicies() {
        return policies;
    }

    public void addProtectedFile(Path filePath) {
        protectedFiles.add(filePath);
    }
    public void removeProtectedFile(Path filePath) {
        protectedFiles.remove(filePath);
    }

    public Set<Path> getProtectedFiles() {
        return protectedFiles;
    }
    public void addProtectedClass(Class clazz) {
        protectedClasses.add(clazz);
    }
    public void removeProtectedClass(Class clazz) {
        protectedClasses.remove(clazz);
    }
    public Set<Class> getProtectedClasses() {
        return protectedClasses;
    }
    public void addClassToBlacklist(Class clazz) {
        classBlacklist.add(clazz);
    }
    public void removeClassFromBlacklist(Class clazz) {
        classBlacklist.remove(clazz);
    }
    public Set<Class> getBlackListedClasses() {
        return classBlacklist;
    }

    public boolean isTriggered() {
        return triggered;
    }

    private SecureEnvironment() {
        // no external instances
    }

}
