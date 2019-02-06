package ee.ttu.java.studenttester.core.security;

import ee.ttu.java.studenttester.core.interfaces.SecurityPolicy;

import java.io.File;
import java.nio.file.Path;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_ANY_FILE_MATCHER;
import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_EXECUTION;
import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_EXIT;
import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_REFLECTION_SHALLOW;
import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_SECURITYMANAGER_CHANGE;
import static ee.ttu.java.studenttester.core.enums.TesterPolicy.DISABLE_TEST_SNIFFING;

public class SecureEnvironment {

    private static final Logger LOG = Logger.getLogger(SecureEnvironment.class.getName());

    private static final SecureEnvironment instance = new SecureEnvironment();

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
            if (Collections.disjoint(stack, classBlacklist) || stack.subList(1, stack.size()).contains(getClass())) {
                // allow everything
                return;
            }
            // if testing with an empty object via API, throw an exception
            // is this safe?
            if (permission == null) {
                throw new SecurityException("Security check failed.");
            }
            // else iterate over all active policies and call their respective methods
            for (var policy : policies) {
                try {
                    policy.getConsumer().accept(permission, stack);
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
     * Sets the default restrictions.
     */
    public void setDefaultRestrictions() {
        addPolicy(DISABLE_EXIT);
        addPolicy(DISABLE_ANY_FILE_MATCHER);
        addPolicy(DISABLE_SECURITYMANAGER_CHANGE);
        addPolicy(DISABLE_REFLECTION_SHALLOW);
        addPolicy(DISABLE_EXECUTION);
        addPolicy(DISABLE_TEST_SNIFFING);
    }


    public void addPolicy(SecurityPolicy policy) {
        policies.add(policy);
    }
    public void removePolicy(SecurityPolicy policy) {
        policies.remove(policy);
    }

    public void addProtectedFile(Path filePath) {
        protectedFiles.add(filePath);
    }
    public void removeProtectedFile(File filePath) {
        protectedFiles.remove(filePath);
    }
    public void addClass(Class clazz) {
        classBlacklist.add(clazz);
    }
    public void removeClass(Class clazz) {
        classBlacklist.remove(clazz);
    }
    public Set<SecurityPolicy> getCurrentPolicies() {
        return policies;
    }
    public Set<Class> getClasses() {
        return classBlacklist;
    }
    public Set<Path> getProtectedFiles() {
        return protectedFiles;
    }
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Holds all the functions responsible for checking various permissions.
     */
    private static Set<SecurityPolicy> policies = new HashSet<>();

    private SecureEnvironment() {

    }

    /**
     * Returns the singleton of this class if the security manager has not been changed or the caller is allowed to
     * access this instance.
     * @return the singleton of this class, if the action was allowed
     */
    public static SecureEnvironment getInstance() {
        // if the security manager has been changed, see if the caller is allowed to access it
        if (securityManager.equals(System.getSecurityManager())) {
            System.getSecurityManager().checkPermission(null);
        }
        return instance;
    }
}
