package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.models.reports.TestNGReport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadUtil;

import java.io.File;

import static ee.ttu.java.studenttester.core.annotations.Identifier.JAR;
import static ee.ttu.java.studenttester.core.enums.TestResultType.FAILED;
import static ee.ttu.java.studenttester.core.enums.TestResultType.PASSED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class JarRunnerTest extends BaseTest {

    private static final int HAS_SKIPPED = 2;
    private static final int CONTAINS_FAILED = 1;
    private static final int SUCCESS = 0; // but not necessarily complete success, as the compiler might have failed

    private static final int TIMEOUT = 1000;

    @BeforeMethod
    private void before() throws Exception {
        initContext();
    }

    @Test
    public void testOneJar() throws Exception {
        moveResource("/JarHello.jar", new File(context.testRoot, "lib"));
        moveResource("/tests/jar/JarHelloInvoker.java", context.tempRoot);
        moveResource("/tests/jar/JarHelloInvokerTest.java", context.testRoot);
        JarRunner runner = new JarRunner(context);
        context.runners.put(JAR, runner);
        runner.run();
        runner.commit();
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();
        assertEquals(context.results.getResultByType(TestNGReport.class).result, RunnerResultType.SUCCESS);
    }

    @Test
    public void testTwoJars() throws Exception {
        moveResource("/JarHello.jar", new File(context.testRoot, "lib"));
        moveResource("/JarBye.jar", new File(context.testRoot, "lib"));
        moveResource("/tests/jar/JarHelloInvokerMulti.java", context.tempRoot);
        moveResource("/tests/jar/JarHelloInvokerMultiTest.java", context.testRoot);
        JarRunner runner = new JarRunner(context);
        context.runners.put(JAR, runner);
        runner.run();
        runner.commit();
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();
        assertEquals(context.results.getResultByType(TestNGReport.class).result, RunnerResultType.SUCCESS);
    }

    private void runNew() throws Exception {
        var runner = new TestNGRunner(context);
        runner.run();
        runner.commit();
    }
}