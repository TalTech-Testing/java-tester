package ee.ttu.java.studenttester.core.helpers;

import ee.ttu.java.studenttester.core.annotations.Gradeable;
import ee.ttu.java.studenttester.core.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.core.enums.ReportMode;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtils {

    /**
     * Gets the custom annotations from a unit test.
     * @param test - unit test to get the metadata from
     * @return annotation data if found
     */
    public static Gradeable getGradeable(final ITestResult test) {
        Method m = test.getMethod().getConstructorOrMethod().getMethod();
        Gradeable g = m.getAnnotation(Gradeable.class);
        if (g == null) {
            return getMockGradeable();
        }
        return g;
    }

    /**
     * Gets the custom annotation from the first annotated
     * class referenced in a test.
     * @param context - context to get the metadata from
     * @return annotation data if found
     */
    public static TestContextConfiguration getClassMetadata(final ITestContext context) {
        TestContextConfiguration a = null;
        for (XmlClass c : context.getCurrentXmlTest().getClasses()) {
            a = (c.getSupportClass()).getAnnotation(TestContextConfiguration.class);
            if (a == null) {
                a = getMockTestContextConfiguration();
            }
        }
        return a;
    }

    public static TestContextConfiguration getMockTestContextConfiguration(
            ReportMode mode, String welcomeMessage, int identifier) {
        return new TestContextConfiguration() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return TestContextConfiguration.class;
            }

            @Override
            public ReportMode mode() {
                return mode;
            }

            @Override
            public String welcomeMessage() {
                return welcomeMessage;
            }

            @Override
            public int identifier() {
                return identifier;
            }
        };
    }

    public static TestContextConfiguration getMockTestContextConfiguration() {
        return getMockTestContextConfiguration(ReportMode.NORMAL, null, -1);
    }

    /**
     * Mock annotation for unit tests that don't have one.
     * @return default annotation for tests
     */
    private static Gradeable getMockGradeable() {
        return new Gradeable() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Gradeable.class;
            }

            @Override
            public int weight() {
                return 1;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public boolean printExceptionMessage() {
                return false;
            }

            @Override
            public boolean printStackTrace() {
                return false;
            }
        };
    }

}
