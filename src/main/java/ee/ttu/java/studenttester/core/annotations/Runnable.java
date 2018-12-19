package ee.ttu.java.studenttester.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Runnable {

    Identifier identifier();

    /**
     * Value that determines the execution order.
     * Smaller values will take precedence.
     */
    int order();

    boolean enabled() default true;

}
