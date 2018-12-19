package ee.ttu.java.studenttester.core.helpers;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.runners.BaseRunner;
import ee.ttu.java.studenttester.core.model.TesterContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContextBuilder {

    private String[] args;

    @Parameter(
            names = {"--runners", "-r"},
            required = true,
            description = "Comma-separated list of actions to run (see below)",
            order = 5
    )
    private List<String> cmdLineIdentifiers;

    public static ContextBuilder builder(String[] args)  {
        ContextBuilder inst = new ContextBuilder();
        inst.args = args;
        JCommander.newBuilder()
                .addObject(inst)
                .acceptUnknownOptions(true) // only seek what runners to init, ignore else
                .build()
                .parse(args);
        return inst;
    }

    public TesterContext buildContext() {
        var context = new TesterContext();
        resolveRunnables(context);
        return context;
    }

    private void resolveRunnables(TesterContext context) {
        Set<Class<?>> runnableClasses = BaseRunner.getRunnableClasses();
        List<String> allIdentifiers = runnableClasses.stream()
                .map(cls -> cls.getAnnotation(Runnable.class).identifier().getValue())
                .collect(Collectors.toList());

        if (!allIdentifiers.containsAll(cmdLineIdentifiers)) {
            cmdLineIdentifiers.removeAll(allIdentifiers);
            throw new IllegalArgumentException(String.format("Unknown runners: %s, known: %s", cmdLineIdentifiers, allIdentifiers));
        }

        var runnerPopulator = JCommander.newBuilder()
                .addObject(this)
                .addObject(context);

        context.runners = runnableClasses.stream()
                .filter(cls -> cmdLineIdentifiers.contains(cls.getAnnotation(Runnable.class).identifier().getValue()))
                .map(cls -> BaseRunner.initRunnerWithContext(cls, context))
                .peek(runnerPopulator::addObject)
                .sorted(Comparator.comparingInt(o -> o.getClass().getAnnotation(Runnable.class).order()))
                .collect(Collectors.toMap(
                            o -> o.getClass().getAnnotation(Runnable.class).identifier(),
                            Function.identity(),
                            (a, b) -> { throw new IllegalStateException("Object collision during initializing runners!"); },
                            LinkedHashMap::new
                        )
                );

        runnerPopulator.build().parse(args);
    }

    public static void generateUsageAndExit() {
        Set<Class<?>> runnableClasses = BaseRunner.getRunnableClasses();

        var dummyContextBuilder = ContextBuilder.builder(new String[] {"-r", "dummy"});
        var dummyContext = new TesterContext();
        dummyContext.close();
        dummyContextBuilder.cmdLineIdentifiers = null; // suppress default values
        dummyContext.tempRoot = null;

        var usageBuilder = JCommander.newBuilder()
                .addObject(dummyContextBuilder)
                .addObject(dummyContext)
                .programName("StudentTester");


        runnableClasses.stream()
                .map(cls -> BaseRunner.initRunnerWithContext(cls, dummyContext))
                .forEach(usageBuilder::addObject);
        var runnableList = runnableClasses.stream()
                .map(cls -> cls.getAnnotation(Runnable.class).identifier().getValue())
                .collect(Collectors.joining(","));

        usageBuilder.build().usage();
        System.out.println(String.format("Runners can be any of the following: %s", runnableList));
        System.exit(0);
    }

    private ContextBuilder() {

    }

}
