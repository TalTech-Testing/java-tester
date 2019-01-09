package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.models.reports.TestNGReport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadUtil;

import static ee.ttu.java.studenttester.core.enums.TestResultType.FAILED;
import static ee.ttu.java.studenttester.core.enums.TestResultType.PASSED;
import static org.testng.Assert.*;

public class TestNGRunnerTest extends BaseTest {

    private static final int HAS_SKIPPED = 2;
    private static final int CONTAINS_FAILED = 1;
    private static final int SUCCESS = 0; // but not necessarily complete success, as the compiler might have failed

    private static final int TIMEOUT = 1000;

    @BeforeMethod
    private void before() {
        initContext();
    }

    @Test
    public void testNoFiles() throws Exception {
        compileAndExpect(RunnerResultType.NOT_RUN);
        runNew();
        assertEquals(context.results.getResultByType(TestNGReport.class).result, RunnerResultType.NOT_RUN);
    }

    @Test
    public void simpleJunit() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);
        assertEquals((int) report.testNGStatus, SUCCESS);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 1.0);
        assertEquals(report.result, RunnerResultType.SUCCESS);
    }

    @Test
    public void simpleJunitBroken() throws Exception {
        moveResource("/tests/calculator/bad/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);
        assertEquals((int) report.testNGStatus, CONTAINS_FAILED);
        assertEquals(report.getTotalPassedCount(), 0);
        assertEquals(report.getTotalGrade(), 0.0);
    }

    @Test
    public void simpleJunitPartiallyBrokenTests() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad.java", context.testRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.PARTIAL_SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);
        assertEquals((int) report.testNGStatus, SUCCESS);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 1.0);
        assertEquals(report.result, RunnerResultType.PARTIAL_SUCCESS);
    }

    @Test
    public void simpleJunitPartiallyIncorrectTests() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad2.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad2.java", context.testRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);
        assertEquals((int) report.testNGStatus, CONTAINS_FAILED);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 0.5);
        assertEquals(report.result, RunnerResultType.SUCCESS);
    }

    @Test
    public void studentTesterAnnotations() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTestAnnotated.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTestAnnotated.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);

        assertEquals((int) report.testNGStatus, CONTAINS_FAILED);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.testContexts.get(0).get(PASSED).get(0).weight, 3);
        assertEquals(report.getTotalPassedWeight(), 3);
        assertEquals(report.getTotalGrade(), 0.6);
        assertEquals(report.result, RunnerResultType.SUCCESS);

        assertEquals(report.testContexts.get(0).get(FAILED).get(0).description, "desc");
        Assert.assertEquals(report.testContexts.get(0).welcomeMessage, "hello");
    }

    @Test
    public void testInfiniteLoop() throws Exception {
        moveResource("/tests/stuck/InfiniteLoop.java", context.tempRoot);
        moveResource("/tests/stuck/InfiniteLoopTest.java", context.tempRoot);
        moveResource("/tests/stuck/InfiniteLoopTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNew();

        var report = context.results.getResultByType(TestNGReport.class);
        assertEquals((int) report.testNGStatus, CONTAINS_FAILED);

        assertFalse(Thread.getAllStackTraces().keySet().stream()
                .anyMatch(t -> t.getName().startsWith(ThreadUtil.THREAD_NAME)));
        //TODO: zombie threads
    }

    private void runNew() throws Exception {
        var runner = new TestNGRunner(context);
        runner.setTimeOut(TIMEOUT);
        runner.run();
        runner.commit();
    }
}