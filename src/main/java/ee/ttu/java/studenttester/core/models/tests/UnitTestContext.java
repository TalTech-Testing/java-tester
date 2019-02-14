package ee.ttu.java.studenttester.core.models.tests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ttu.java.studenttester.annotations.Gradable;
import ee.ttu.java.studenttester.core.helpers.AnnotationUtils;
import ee.ttu.java.studenttester.enums.ReportMode;
import ee.ttu.java.studenttester.core.enums.TestResultType;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.*;
import java.util.stream.Collectors;

import static ee.ttu.java.studenttester.core.enums.TestResultType.FAILED;
import static ee.ttu.java.studenttester.core.enums.TestResultType.PASSED;
import static ee.ttu.java.studenttester.core.enums.TestResultType.SKIPPED;

@SuppressWarnings("WeakerAccess")
public class UnitTestContext {

    @JsonIgnore
    public ITestContext originalContext;

    public List<SingleResult> unitTests = new ArrayList<>();

    public String name;
    public String file;
    public Date startDate;
    public Date endDate;

    //TestContextConfiguration
    public ReportMode mode;
    public String welcomeMessage = "";
    public int identifier;

    public void buildFromOriginal() {
        name = originalContext.getName();
        file = originalContext.getCurrentXmlTest().getClasses().get(0).getName();
        startDate = originalContext.getStartDate();
        endDate = originalContext.getEndDate();

        // iterate over three result types
        var allResults = new HashSet<ITestResult>();
        allResults.addAll(originalContext.getPassedTests().getAllResults());
        allResults.addAll(originalContext.getFailedTests().getAllResults());
        allResults.addAll(originalContext.getSkippedTests().getAllResults());

        for (ITestResult testNGResult : allResults) {
            Gradable testMetadata = AnnotationUtils.getGradable(testNGResult);

            var unitTestResult = new SingleResult();

            unitTestResult.description = testMetadata.description();
            unitTestResult.printExceptionMessage = testMetadata.printExceptionMessage();
            unitTestResult.printStackTrace = testMetadata.printStackTrace();
            unitTestResult.weight = testMetadata.weight();

            unitTestResult.originalResult = testNGResult;
            unitTestResult.parentContext = this;
            unitTestResult.buildFromOriginal();

            unitTests.add(unitTestResult);
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getCount() {
        return unitTests.size();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getPassedCount() {
        return getCount(PASSED);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getGrade() {
        try {
            return 100.0 * getWeight(PASSED) / getWeight();
        } catch (ArithmeticException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getWeight() {
        return getWeight(PASSED) + getWeight(FAILED) + getWeight(SKIPPED);
    }

    @JsonIgnore
    public int getWeight(TestResultType resultType) {
        return unitTests.stream()
                .filter(r -> r.status == resultType)
                .map(r -> r.weight)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @JsonIgnore
    public int getCount(TestResultType resultType) {
        return (int) unitTests.stream()
                .filter(r -> r.status == resultType)
                .count();
    }

    @JsonIgnore
    public List<SingleResult> get(TestResultType resultType) {
        return unitTests.stream()
                .filter(r -> r.status == resultType)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        try {
            var builder = new StringBuilder();

            builder.append(String.format("\n%s\n%s\n", name, endDate));
            if (!"".equals(welcomeMessage) && welcomeMessage != null) {
                builder.append(String.format("%s\n", welcomeMessage));
            }
            builder.append(" ---\n");

            unitTests.forEach(builder::append);

            if (mode != ReportMode.MUTED) {
                builder.append(String.format("\nPassed unit tests: %d/%d\n"
                                + "Failed unit tests: %d\n"
                                + "Skipped unit tests: %d\n"
                                + "Grade: %.1f%%\n",
                        getCount(PASSED), getCount(), getCount(FAILED), getCount(SKIPPED), getGrade()));
            } else {
                builder.append("Unit tests were run, but no output will be shown.\n");
            }

            return builder.toString();
        } catch (NullPointerException e) {
            return super.toString() + " (incomplete object)";
        }
    }
}
