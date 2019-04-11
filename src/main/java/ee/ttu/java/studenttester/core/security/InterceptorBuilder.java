package ee.ttu.java.studenttester.core.security;

import ee.ttu.java.studenttester.core.interceptors.ByteBuddyLogInterceptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ee.ttu.java.studenttester.core.security.SecureEnvironment.checkAccess;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class InterceptorBuilder {

    private static final Logger LOG = Logger.getLogger(InterceptorBuilder.class.getName());

    public static final String SET_ACCESSIBLE = "setAccessible";
    public static final String SET_ACCESSIBLE0 = "setAccessible0";

    public static final Class<RevisedAccessibleObject> CLASS_CONTAINING_REPLACEMENT = RevisedAccessibleObject.class;
    public static final Class<ExtReflectPermission> EXTENDED_REFLECT_PERMISSION = ExtReflectPermission.class;

    private static Instrumentation instance;
    private static Map<Method, ClassFileTransformer> transformerMap = new HashMap<>();

    private static Method setAccessible0;
    private static Method setAccessibleArray;

    private static Method constrSetAccessible;
    private static Method fieldSetAccessible;
    private static Method methodSetAccessible;
    private static Method accObjSetAccessible;

    private static List<Method> setAccessibleImpls;

    static {
        try {
            setAccessible0 = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE0, boolean.class);
            setAccessibleArray = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE, AccessibleObject[].class, boolean.class);

            constrSetAccessible = Constructor.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
            fieldSetAccessible = Field.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
            methodSetAccessible = Method.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);
            accObjSetAccessible = AccessibleObject.class.getDeclaredMethod(SET_ACCESSIBLE, boolean.class);

            setAccessibleImpls = List.of(constrSetAccessible, fieldSetAccessible, methodSetAccessible, accObjSetAccessible);
        } catch (NoSuchMethodException e) {
            throw new SecurityException(InterceptorBuilder.class.getName() + " initialization failed!", e);
        }
    }

    public static void installAgent() {
        checkAccess();
        if (instance == null) {
            instance = ByteBuddyAgent.install();
        } else {
            LOG.warning("Agent already installed");
        }
    }

    /**
     * Pass execution flow from the default setAccessible implementations to the custom ones.
     */
    public static void initDefaultTransformations() {
        checkAccess();
        loadClassBootstrapClassLoader(List.of(EXTENDED_REFLECT_PERMISSION, CLASS_CONTAINING_REPLACEMENT));
        setAccessibleImpls.forEach(method ->
                InterceptorBuilder
                        .transformMethod(CLASS_CONTAINING_REPLACEMENT, false, method, setAccessible0, 0));
        InterceptorBuilder.transformMethod(CLASS_CONTAINING_REPLACEMENT, false, setAccessibleArray, null);
    }

    /**
     * Transforms a method by substituting it another found in the specified class. Optionally define the method
     * to be executed after the transformed one.
     * @param classContainingReplacement class containing a method with a suitable signature
     * @param needsLoading determine whether to load this class using the bootstrap class loader
     * @param methodToReplace the method to replace
     * @param nextMethod the method to call after the replaced one has finished
     * @param nextMethodArgs arguments by index to pass to the next method
     * @return class file transformer
     */
    public static ClassFileTransformer transformMethod(Class classContainingReplacement, boolean needsLoading,
                                     Method methodToReplace, Method nextMethod, int... nextMethodArgs) {
        checkAccess();
        if (instance == null) {
            throw new NullPointerException("Instance not loaded");
        }

        if (transformerMap.containsKey(methodToReplace)) {
            return transformerMap.get(methodToReplace);
        }

        if (needsLoading) {
            loadClassBootstrapClassLoader(List.of(classContainingReplacement));
        }

        var listener = new ByteBuddyLogInterceptor();
        ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);

        var transformer = new AgentBuilder.Default()
                .with(byteBuddy)
                //.with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                //.with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .disableClassFormatChanges()
                //.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(listener)
                //.enableBootstrapInjection(instance, tempJarLocation)
                .ignore(none())
                .type(ElementMatchers.is(methodToReplace.getDeclaringClass()))
                .transform((b, c, d, e) -> b.method(ElementMatchers.named(methodToReplace.getName())
                        .and(takesArguments(methodToReplace.getParameterTypes())))
                        .intercept(getMethodDelegation(classContainingReplacement, nextMethod, nextMethodArgs)))
                .installOn(instance);
        transformerMap.put(methodToReplace, transformer);
        return transformer;
    }

    private static Implementation.Composable getMethodDelegation(Class replacementClass,
                                                                 Method nextMethod, int... nextMethodArgs) {
        Implementation.Composable del = MethodDelegation.to(replacementClass);
        if (nextMethod != null && nextMethodArgs != null) {
            del = del.andThen(MethodCall.invoke(nextMethod).withArgument(nextMethodArgs));
        } else if (nextMethod != null) {
            del = del.andThen(MethodCall.invoke(nextMethod));
        }
        return del;
    }

    public static void removeAllTransformations() {
        checkAccess();
        transformerMap.values().forEach(instance::removeTransformer);
    }

    public static void loadClassBootstrapClassLoader(List<Class> classes) {
        checkAccess();
        loadClassBootstrapClassLoader(classes, FileUtils.getTempDirectory());
    }

    public static void loadClassBootstrapClassLoader(List<Class> classes, File tempJarLocation) {
        checkAccess();
        ClassLoader platformClassLoader = ClassLoader.getPlatformClassLoader();
        Map<TypeDescription.ForLoadedType, byte[]> unloaded = classes.stream()
                .filter(clazz -> {
                    try {
                        platformClassLoader.loadClass(clazz.getName());
                    } catch (ClassNotFoundException e) {
                        // not loaded
                        return true;
                    }
                    // loaded, skip
                    return false;
                })
                .collect(Collectors
                        .toMap(TypeDescription.ForLoadedType::new, ClassFileLocator.ForClassLoader::read));
        if (unloaded.size() > 0) {
            ClassInjector.UsingInstrumentation
                    .of(tempJarLocation, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instance)
                    .inject(unloaded);
        }
    }

    private InterceptorBuilder() {
        // no instances
    }
}
