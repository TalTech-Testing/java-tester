package ee.ttu.java.studenttester.core;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.ContextBuilder;
import ee.ttu.java.studenttester.core.helpers.FileArgsReader;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.validators.FileParameterValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class StudentTester {

    private static final Logger LOG = Logger.getLogger(StudentTester.class.getName());

    private TesterContext context;
    private static boolean running = false;

    @Parameter(names = {"--help", "-h"})
    private boolean printUsage = false;

    @Parameter(
            names = {"--inputArgs", "-i"},
            description = "Input file to be used in place of other arguments.",
            order = 10,
            validateValueWith = FileParameterValidator.class
    )
    private File inputFile;

    public void run() {
        if (running) {
            throw new StudentTesterException("Cannot run multiple instances at the same time!");
        }
        running = true;
        for (var runnable : context.runners.values()) {
            try {
                runnable.run();
                runnable.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        running = false;
        if (context.cleanup) {
            context.close();
        }
    }

    public static void main(String... args) {
        long start = System.currentTimeMillis();
        LOG.info("Running StudentTester, build " + StudentTester.class.getPackage().getImplementationVersion());
        StudentTester tester = new StudentTester();
        JCommander.newBuilder()
                .addObject(tester)
                .acceptUnknownOptions(true) // only check if help is specified
                .build()
                .parse(args);
        if (args.length == 0 || tester.printUsage) {
            ContextBuilder.generateUsageAndExit();
        } else if (tester.inputFile != null) {
            var stdinArgs = FileArgsReader.getArgsFromFile(tester.inputFile);
            var argsList = new ArrayList<>(Arrays.asList(args));
            argsList.addAll(Arrays.asList(stdinArgs));
            args = argsList.toArray(String[]::new);
        }
        LOG.info("Program arguments: " + Arrays.asList(args));
        tester.context = ContextBuilder.builder(args).buildContext();
        tester.run();
        LOG.info(String.format("Finished running, elapsed time %dms", System.currentTimeMillis() - start));
    }

}
