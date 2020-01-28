package ee.ttu.java.studenttester.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.*;

/**
 * Annotation for tests.
 * @author Andres
 *
 */
@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Gradable {

    /**
     * Field for test weight.
     * @return default weight 1
     */
    @JsonProperty
    int weight() default 1;

    /**
     * Field for test description.
     * @return default description
     */
    @JsonProperty
    String description() default "";

    /**
     * Determines whether detailed exception message
     * will be printed.
     * @return default false
     */
    @JsonProperty
    boolean printExceptionMessage() default true;
    /**
     * Determines whether stack trace
     * will be printed.
     * @return default false
     */
    @JsonProperty
    boolean printStackTrace() default false;
    
}