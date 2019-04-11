package ee.ttu.java.studenttester.core.security;

import java.security.BasicPermission;

/**
 * Custom access permission to identify access to StudentTester classes
 */
public class StudentTesterAccessPermission extends BasicPermission {

    public StudentTesterAccessPermission(String name) {
        super(name);
    }

}
