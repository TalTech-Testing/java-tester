package ee.ttu.java.studenttester.core;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.hodor.Shim;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.ContextBuilder;
import ee.ttu.java.studenttester.core.models.TesterContext;

import java.util.logging.Logger;

public class StudentTester {

    private static final Logger LOG = Logger.getLogger(StudentTester.class.getName());

    private TesterContext context;
    private static boolean running = false;

    @Parameter(names = {"--help", "-h"})
    private boolean printUsage = false;

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
        // check if arguments passed via stdin in special format
        var stdinArgs = Shim.getArgsFromStdin();
        if (stdinArgs != null) {
            args = stdinArgs;
        }

        long start = System.currentTimeMillis();
        StudentTester tester = new StudentTester();
        JCommander.newBuilder()
                .addObject(tester)
                .acceptUnknownOptions(true) // only check if help is specified
                .build()
                .parse(args);
        if (args.length == 0 || tester.printUsage) {
            ContextBuilder.generateUsageAndExit();
        }
        LOG.info("Running StudentTester, build " + StudentTester.class.getPackage().getImplementationVersion());
        tester.context = ContextBuilder.builder(args).buildContext();
        tester.run();
        LOG.info(String.format("Finished running, elapsed time %dms", System.currentTimeMillis() - start));
    }

}
