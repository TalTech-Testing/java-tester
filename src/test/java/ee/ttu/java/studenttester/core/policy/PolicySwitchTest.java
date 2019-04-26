package ee.ttu.java.studenttester.core.policy;

import ee.ttu.java.studenttester.core.BaseTest;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.enums.TestResultType;
import ee.ttu.java.studenttester.core.models.reports.TestNGReport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertSame;

public class PolicySwitchTest extends BaseTest {

    @BeforeMethod
    private void before() {
        initContext();
    }

    @Test
    public void testDisableDefaultPolicy() throws Exception {
        moveResource("/tests/policy/PolicyCheckSwitch.java", context.contentRoot);
        moveResource("/tests/policy/PolicyCheckSwitch.java", context.tempRoot);
        moveResource("/tests/policy/PolicyCheckSwitchTest.java", context.tempRoot);
        moveResource("/tests/policy/PolicyCheckSwitchTest.java", context.testRoot);
        compileAndExpect(RunnerResultType.SUCCESS);
        runNewTestNG();

        var report = context.results.getResultByType(TestNGReport.class);

        report.testContexts.get(0).unitTests.forEach(singleResult ->
                assertSame(singleResult.status, TestResultType.PASSED, singleResult.stackTrace));
    }

}
