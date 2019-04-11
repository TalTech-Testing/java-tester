package ee.ttu.java.studenttester.core.interceptors;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class ByteBuddyLogInterceptor extends AgentBuilder.Listener.Adapter {

    private boolean failure;
    private Throwable throwable;

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
        super.onTransformation(typeDescription, classLoader, module, loaded, dynamicType);
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        super.onIgnored(typeDescription, classLoader, module, loaded);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        super.onComplete(typeName, classLoader, module, loaded);
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader,
                        JavaModule javaModule, boolean loaded, Throwable throwable) {
        this.failure = true;
        this.throwable = throwable;
    }

    public boolean hasFailure() {
        return failure;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
