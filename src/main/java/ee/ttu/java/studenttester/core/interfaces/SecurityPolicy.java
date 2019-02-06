package ee.ttu.java.studenttester.core.interfaces;

import java.security.Permission;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Interface for creating custom policies.
 */
public interface SecurityPolicy {
    /**
     * Gets a method that evaluates whether the permission object should be allowed or not.
     * @return consumer that accepts a permission object and a class list (stack)
     */
    BiConsumer<Permission, List<Class>> getConsumer();
}
