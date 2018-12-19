package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.model.TesterContext;
import org.reflections.Reflections;

import java.util.Set;
import java.util.logging.Logger;

public abstract class BaseRunner {

    protected final Logger LOG = Logger.getLogger(getClass().getName());

    public static final String[] JAVA_FILTER = new String[] { "java" };

    TesterContext context;

    BaseRunner(TesterContext context) {
        this.context = context;
    }

    /**
     * Runs the actions that this class is supposed to do.
     * @throws Exception
     */
    public abstract void run() throws Exception;

    /**
     * Method to commit any results to the context.
     * This method is invoked after run().
     */
    public abstract void commit();

    public static Set<Class<?>> getRunnableClasses() {
        return new Reflections(BaseRunner.class.getPackageName())
                .getTypesAnnotatedWith(Runnable.class);
    }

    @SuppressWarnings("unchecked")
    public static BaseRunner initRunnerWithContext(Class<?> runnableClass, TesterContext context) {
        try {
            return ((Class<BaseRunner>) runnableClass)
                    .getDeclaredConstructor(TesterContext.class)
                    .newInstance(context);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke a constructor", e);
        }
    }

}
