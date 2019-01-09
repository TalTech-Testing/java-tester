package ee.ttu.java.studenttester.core.helpers;

import ee.ttu.java.studenttester.core.enums.TestClassType;

import java.io.File;
import java.lang.reflect.Method;

import static ee.ttu.java.studenttester.core.enums.TestClassType.*;

public class ClassUtils {

    public static String relativizeFilePath(final File file, final File relativizeAgainst) {
        return relativizeAgainst.toURI().relativize(file.toURI()).getPath();
    }

    /**
     * Converts a file path to a classpath.
     * @param file .java target file
     * @param relativizeAgainst root folder of the project to determine the classpath
     * @return classpath (some.path.File)
     */
    public static String filePathToClassPath(final File file, final File relativizeAgainst) {
        return relativizeFilePath(file, relativizeAgainst)
                .replace(".java", "")
                .replace("/", ".");
    }

    /**
     * Quick test to find out whether the class is a JUnit test or a TestNG test.
     * The class must already be compiled and in the classpath.
     * @param testClassFile .java class file to check
     * @param relativizeAgainst root folder of the project to determine the classpath
     * @return the type of class.
     * @throws ClassNotFoundException when something goes wrong
     */
    public static TestClassType getClassType(final File testClassFile, final File relativizeAgainst) throws ClassNotFoundException {
        var classToTest = Class.forName(filePathToClassPath(testClassFile, relativizeAgainst));
        return getClassType(classToTest);
    }

    /**
     * Quick test to find out whether the class is a JUnit test or a TestNG test.
     * @param classToTest the class to test
     * @return the type of class.
     */
    public static TestClassType getClassType(final Class<?> classToTest) {
        boolean testNGFound = false, junitFound = false;
        // if the class contains at least one
        for (Method unitTest : classToTest.getDeclaredMethods()) {
            // JUnit method, assume it's a JUnit test
            if (unitTest.isAnnotationPresent(org.junit.Test.class)) {
                junitFound = true;
                // TestNG method, assume it's a TestNG test
            } else if (unitTest.isAnnotationPresent(org.testng.annotations.Test.class)) {
                testNGFound = true;
            }
        }
        if (junitFound && testNGFound) {
            return MIXED;
        } else if (junitFound) {
            return JUNIT;
        } else if (testNGFound) {
            return TESTNG;
        } else {
            return NOT_TEST_CLASS;
        }
    }
}
