package ee.ttu.java.studenttester.core.helpers;

import ee.ttu.java.studenttester.core.enums.SourceSetType;
import ee.ttu.java.studenttester.core.enums.TestClassType;
import ee.ttu.java.studenttester.core.security.ExtReflectPermission;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.Permission;

import static ee.ttu.java.studenttester.core.enums.TestClassType.*;

public class ClassUtils {

    /**
     * Converts a file to its relative path compared to some
     * @param file file to use
     * @param relativizeAgainst directory to use as base
     * @param sourceSetType remove "src" or "src/main/java" etc if defined
     * @return relative path as string
     */
    public static String relativizeFilePath(final File file, final File relativizeAgainst, SourceSetType sourceSetType) {
        String path = relativizeAgainst.toURI().relativize(file.toURI()).getPath();
        if (sourceSetType == SourceSetType.SRC_MAIN_JAVA) {
            return path.replaceFirst("src\\/(main|test|resources)\\/java\\/", "");
        } else if (sourceSetType == SourceSetType.SRC) {
            return path.replaceFirst("src\\/", "");
        }
        return path;
    }

    /**
     * Converts a file to its relative path compared to some directory.
     * @param file file to use
     * @param relativizeAgainst directory to use as base
     * @return relative path as string
     */
    public static String relativizeFilePath(final File file, final File relativizeAgainst) {
        return relativizeFilePath(file, relativizeAgainst, SourceSetType.ROOT);
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

    private static Class<ExtReflectPermission> extReflectPermissionClass;
    private static Field accessibleObject, accessibleObjectFlag;

    @SuppressWarnings("unchecked")
    public static ExtReflectPermission getExtReflectPermission(Permission p) {
        // here, bare ExtReflectPermission is not the one loaded by Byte Buddy, we need to explicitly load that version
        if (extReflectPermissionClass == null) {
            try {
                extReflectPermissionClass = (Class<ExtReflectPermission>) ClassLoader.getPlatformClassLoader()
                        .loadClass(ExtReflectPermission.class.getName());
                accessibleObject = extReflectPermissionClass.getDeclaredField("accessibleObject");
                accessibleObjectFlag = extReflectPermissionClass.getDeclaredField("flag");
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                throw new IllegalStateException("Cannot load " + ExtReflectPermission.class.getName() + " from platform loader", e);
            }
        }

        if (extReflectPermissionClass.isInstance(p)) {
            try {
                return new ExtReflectPermission((AccessibleObject) accessibleObject.get(p), accessibleObjectFlag.getBoolean(p));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot load " + ExtReflectPermission.class.getName() + " variable", e);
            }
        }

        return null;
    }

    public static Class getTopLevelClass(Class possibleInnerClass) {
        Class topLevelClass = possibleInnerClass;
        while (topLevelClass.getEnclosingClass() != null) {
            topLevelClass = topLevelClass.getEnclosingClass();
        }
        return topLevelClass;
    }

    private ClassUtils() {

    }
}
