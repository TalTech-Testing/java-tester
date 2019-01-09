package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.models.reports.CheckStyleReport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CheckStyleRunnerTest extends BaseTest {

    @BeforeMethod
    private void before() {
        initContext();
    }

    @Test
    public void testNoFiles() throws Exception {
        runNew();

        var report = context.results.getResultByType(CheckStyleReport.class);

        assertEquals(report.checkStyleErrorCount, 0);
        assertEquals(report.errors.size(), 0);
    }

    @Test
    public void testSimple() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.contentRoot);
        runNew();

        var report = context.results.getResultByType(CheckStyleReport.class);

        assertTrue(report.checkStyleErrorCount > 0);
        assertEquals(report.errors.stream().map(e -> e.fileName).distinct().count(), 1);
        assertEquals(report.checkStyleErrorCount, report.errors.size());
    }

    private void runNew() {
        var runner = new CheckStyleRunner(context);
        runner.run();
        runner.commit();
    }

}