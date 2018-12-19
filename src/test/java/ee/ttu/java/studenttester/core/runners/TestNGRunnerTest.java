package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.CompilationResult;
import ee.ttu.java.studenttester.core.model.reports.TestNGReport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import org.testng.internal.thread.ThreadUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestNGRunnerTest extends BaseTest {

    private static final int HAS_SKIPPED = 2;
    private static final int HAS_FAILED = 1;
    private static final int SUCCESS = 0; // but not necessarily complete success, as the compiler might have failed

    private static final int TIMEOUT = 1000;

    @BeforeMethod
    private void before() {
        initContext();
    }

    @Test
    public void testNoFiles() throws Exception {
        compileAndExpect(CompilationResult.FAILURE);
        runNew();
        assertFalse(context.results.containsKey(Identifier.TESTNG));
    }

    @Test
    public void simpleJunit() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(CompilationResult.SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);
        assertEquals((int) report.status, SUCCESS);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 1.0);
        assertFalse(report.incompleteGrade);
    }

    @Test
    public void simpleJunitBroken() throws Exception {
        moveResource("/tests/calculator/bad/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(CompilationResult.SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);
        assertEquals((int) report.status, HAS_FAILED);
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
        compileAndExpect(CompilationResult.PARTIAL_SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);
        assertEquals((int) report.status, SUCCESS);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 1.0);
        assertTrue(report.incompleteGrade);
    }

    @Test
    public void simpleJunitPartiallyIncorrectTests() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad2.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.tempRoot);
        moveResource("/tests/calculator/bad/CalculatorTestBad2.java", context.testRoot);
        moveResource("/tests/calculator/CalculatorTest.java", context.testRoot);
        compileAndExpect(CompilationResult.SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);
        assertEquals((int) report.status, HAS_FAILED);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.getTotalGrade(), 0.5);
        assertFalse(report.incompleteGrade);
    }

    @Test
    public void studentTesterAnnotations() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTestAnnotated.java", context.tempRoot);
        moveResource("/tests/calculator/CalculatorTestAnnotated.java", context.testRoot);
        compileAndExpect(CompilationResult.SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);

        assertEquals((int) report.status, HAS_FAILED);
        assertEquals(report.getTotalPassedCount(), 1);
        assertEquals(report.testContexts.get(0).passed.get(0).metadata.weight(), 3);
        assertEquals(report.getTotalPassedWeight(), 3);
        assertEquals(report.getTotalGrade(), 0.6);
        assertFalse(report.incompleteGrade);

        assertEquals(report.testContexts.get(0).failed.get(0).metadata.description(), "desc");
        Assert.assertEquals(report.testContexts.get(0).configuration.welcomeMessage(), "hello");
    }

    @Test
    public void testInfiniteLoop() throws Exception {
        moveResource("/tests/stuck/InfiniteLoop.java", context.tempRoot);
        moveResource("/tests/stuck/InfiniteLoopTest.java", context.tempRoot);
        moveResource("/tests/stuck/InfiniteLoopTest.java", context.testRoot);
        compileAndExpect(CompilationResult.SUCCESS);
        runNew();

        var report = (TestNGReport) context.results.get(Identifier.TESTNG);
        assertEquals((int) report.status, HAS_FAILED);

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