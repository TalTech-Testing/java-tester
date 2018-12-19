package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.model.reports.CheckStyleReport;
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

        var report = (CheckStyleReport) context.results.get(Identifier.CHECKSTYLE);

        assertEquals(report.checkStyleErrorCount, 0);
        assertEquals(report.checkStyleResultMap.size(), 0);
    }

    @Test
    public void testSimple() throws Exception {
        moveResource("/tests/calculator/Calculator.java", context.contentRoot);
        runNew();

        var report = (CheckStyleReport) context.results.get(Identifier.CHECKSTYLE);

        assertTrue(report.checkStyleErrorCount > 0);
        assertEquals(report.checkStyleResultMap.size(), 1);
        assertEquals(report.checkStyleErrorCount, report.checkStyleResultMap.values().iterator().next().size());
    }

    private void runNew() {
        var runner = new CheckStyleRunner(context);
        runner.run();
        runner.commit();
    }

}