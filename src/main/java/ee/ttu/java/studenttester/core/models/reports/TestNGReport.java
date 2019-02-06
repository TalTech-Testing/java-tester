package ee.ttu.java.studenttester.core.models.reports;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.enums.ReportMode;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.enums.TestResultType;
import ee.ttu.java.studenttester.core.interceptors.TestNGInterceptor;
import ee.ttu.java.studenttester.core.models.tests.UnitTestContext;

import java.util.ArrayList;
import java.util.List;

public class TestNGReport extends AbstractReport {

    @Override
    public Identifier getIdentifier() {
        return Identifier.TESTNG;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @JsonIgnore
    public TestNGInterceptor resultListener = new TestNGInterceptor();

    @JsonIgnore
    public Integer testNGStatus;

    public List<UnitTestContext> testContexts = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTotalCount() {
        return testContexts.stream()
                .map(UnitTestContext::getCount)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTotalPassedCount() {
        return testContexts.stream()
                .map(c -> c.getCount(TestResultType.PASSED))
                .mapToInt(Integer::intValue)
                .sum();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getTotalGrade() {
        try {
            return 1.0 * getTotalPassedWeight()
                    / testContexts.stream()
                    .map(UnitTestContext::getWeight)
                    .mapToInt(Integer::intValue)
                    .sum();
        } catch (ArithmeticException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @JsonIgnore
    public int getTotalPassedWeight() {
        return testContexts.stream()
                .map(c -> c.getWeight(TestResultType.PASSED))
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Override
    public String toString() {
        var globalOutputBuilder = new StringBuilder()
                .append("* Unit tests *\n\n");

        testContexts.forEach(globalOutputBuilder::append);

        if (testContexts.isEmpty()) {
            globalOutputBuilder.append("There are no results.\n");
        } else if (testContexts.stream().noneMatch(c -> c.mode == ReportMode.MUTED)) {
            globalOutputBuilder.append(String.format("\nOverall grade: %.1f%%%s\n", getTotalGrade() * 100,
                    result != RunnerResultType.SUCCESS ? "*\nFinal result based on only successfully compiled tests" : ""));
        }

        return globalOutputBuilder.toString();
    }
}
