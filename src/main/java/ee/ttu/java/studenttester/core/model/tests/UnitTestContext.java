package ee.ttu.java.studenttester.core.model.tests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.ttu.java.studenttester.core.annotations.Gradeable;
import ee.ttu.java.studenttester.core.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.core.helpers.AnnotationUtils;
import ee.ttu.java.studenttester.core.enums.ReportMode;
import ee.ttu.java.studenttester.core.enums.ResultType;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static ee.ttu.java.studenttester.core.enums.ResultType.FAILED;
import static ee.ttu.java.studenttester.core.enums.ResultType.PASSED;

@SuppressWarnings("WeakerAccess")
public class UnitTestContext {

    @JsonIgnore
    public ITestContext originalContext;

    public List<SingleResult> passed = new ArrayList<>();
    public List<SingleResult> failed = new ArrayList<>();
    public List<SingleResult> skipped = new ArrayList<>();

    public String name;
    public String file;
    public Date startDate;
    public Date endDate;

    public TestContextConfiguration configuration;

    public void buildFromOriginal() {
        name = originalContext.getName();
        file = originalContext.getCurrentXmlTest().getClasses().get(0).getName();
        startDate = originalContext.getStartDate();
        endDate = originalContext.getEndDate();

        var unitTestNotes = new ArrayList<String>(); // clear or initialize diagnostic array
        Set<ITestResult> testsFromContext;

        // iterate over three result types
        for (ResultType type : ResultType.values()) {
            if (type == PASSED) {
                testsFromContext = originalContext.getPassedTests().getAllResults();
            } else if (type == FAILED) {
                testsFromContext = originalContext.getFailedTests().getAllResults();
            } else {
                testsFromContext = originalContext.getSkippedTests().getAllResults();
            }

            for (ITestResult testNGResult : testsFromContext) {
                Gradeable testMetadata = AnnotationUtils.getGradeable(testNGResult);

                var unitTestResult = new SingleResult();
                unitTestResult.metadata = testMetadata;
                unitTestResult.originalResult = testNGResult;
                unitTestResult.parentContext = this;
                unitTestResult.buildFromOriginal();

                if (type == PASSED) {
                    passed.add(unitTestResult);
                } else if (type == FAILED) {
                    failed.add(unitTestResult);
                } else {
                    skipped.add(unitTestResult);
                }
            }
        }
    }

    public int getCount() {
        return passed.size() + failed.size() + skipped.size();
    }

    public int getPassedCount() {
        return passed.size();
    }

    public double getGrade() {
        try {
            return 1.0 * getWeight(passed) / getWeight();
        } catch (ArithmeticException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getWeight() {
        return getWeight(passed) + getWeight(failed) + getWeight(skipped);
    }

    private int getWeight(List<SingleResult> unitTests) {
        return unitTests.stream()
                .map(r -> r.metadata)
                .map(Gradeable::weight)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @JsonIgnore
    public int getPassedWeight() {
        return getWeight(passed);
    }

    @JsonIgnore
    public List<SingleResult> getAllResults() {
        var list = new ArrayList<SingleResult>();
        list.addAll(passed);
        list.addAll(failed);
        list.addAll(skipped);
        return list;
    }

    @Override
    public String toString() {
        try {
            var builder = new StringBuilder();

            builder.append(String.format("\n%s\n%s\n", name, endDate));
            if (configuration != null && !"".equals(configuration.welcomeMessage())) {
                builder.append(String.format("%s\n", configuration.welcomeMessage()));
            }
            builder.append(" ---\n");

            passed.forEach(builder::append);
            failed.forEach(builder::append);
            skipped.forEach(builder::append);

            if (configuration.mode() != ReportMode.MUTED) {
                builder.append(String.format("\nPassed unit tests: %d/%d\n"
                                + "Failed unit tests: %d\n"
                                + "Skipped unit tests: %d\n"
                                + "Grade: %.1f%%\n",
                        passed.size(), getCount(), failed.size(), skipped.size(), getGrade() * 100));
            } else {
                builder.append("Unit tests were run, but no output will be shown.\n");
            }

            return builder.toString();
        } catch (NullPointerException e) {
            return super.toString() + " (incomplete object)";
        }
    }
}
