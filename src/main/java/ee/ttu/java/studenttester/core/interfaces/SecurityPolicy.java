package ee.ttu.java.studenttester.core.interfaces;

import ee.ttu.java.studenttester.core.security.PermissionContext;

import java.security.Permission;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Interface for creating custom policies.
 */
public interface SecurityPolicy {
    /**
     * Gets a method that evaluates whether the permission object should be allowed or not.
     * @return consumer that accepts a permission context containing the permission, stack and security environment
     */
    Consumer<PermissionContext> getConsumer();
}
