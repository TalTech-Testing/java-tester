package ee.ttu.java.studenttester.annotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ttu.java.studenttester.core.enums.TesterPolicy;
import ee.ttu.java.studenttester.enums.ReportMode;

import java.lang.annotation.*;

/**
 * Annotation for defining some global class settings.
 * @author Andres
 *
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface TestContextConfiguration {

    /**
     * DummyTest class verbosity level.
     * @return verbosity level
     */
    @JsonProperty
    ReportMode mode() default ReportMode.NORMAL;

    /**
     * Text to be displayed before tests.
     * @return message
     */
    @JsonProperty
    String welcomeMessage() default "";

    /**
     * A number that identifies this test and
     * should be unique and non-negative.
     * Default value is -1.
     * @return indentifier number
     */
    @JsonProperty
    int identifier() default -1;

    /**
     * Defines a list of explicitly enabled policies.
     * Other policies from the default set will also be active.
     * @return list of explicitly enabled policies
     */
    TesterPolicy[] enablePolicies() default {};

    /**
     * Defines a list of explicitly disabled policies.
     * Other policies from the default will still be active.
     * @return list of explicitly disabled policies
     */
    TesterPolicy[] disablePolicies() default {};

}