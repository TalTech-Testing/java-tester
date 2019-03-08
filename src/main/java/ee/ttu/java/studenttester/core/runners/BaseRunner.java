package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.models.TesterContext;
import org.reflections.Reflections;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.logging.Logger;

public abstract class BaseRunner {

    protected final Logger LOG = Logger.getLogger(getClass().getName());

    public static final String[] JAVA_FILTER = new String[] { "java" };

    protected TesterContext context;
    private ClassLoader classLoader;

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

    /**
     * Gets the class loader associated with the temporary directory.
     * @return class loader
     */
    protected ClassLoader getTempClassLoader(ClassLoader parent, boolean reinitialize) throws MalformedURLException {
        URL[] urls = {context.tempRoot.toURI().toURL()};
        if (reinitialize) {
            if (parent == null) {
                classLoader = URLClassLoader.newInstance(urls);
            } else {
                classLoader = URLClassLoader.newInstance(urls, parent);
            }
        }
        return classLoader;
    }

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
