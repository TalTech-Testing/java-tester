package ee.ttu.java.studenttester.core.models.tests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.ttu.java.studenttester.core.enums.TestResultType;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ee.ttu.java.studenttester.core.enums.ReportMode.*;

@SuppressWarnings("WeakerAccess")
public class SingleResult {

    @JsonIgnore
    public UnitTestContext parentContext;

    @JsonIgnore
    public ITestResult originalResult;

    public TestResultType status = TestResultType.OTHER;

    // GRADEABLE
    public int weight;
    public String description;
    public boolean printExceptionMessage;
    public boolean printStackTrace;

    public long timeElapsed;

    //public List<String> publicNotes = new ArrayList<>();
    //public List<String> privateNotes = new ArrayList<>();
    public List<String> groupsDependedUpon = new ArrayList<>();
    public List<String> methodsDependedUpon = new ArrayList<>();

    public String name;
    public String stackTrace;
    public String exceptionClass;
    public String exceptionMessage;

    public List<Output> stdout = new ArrayList<>();
    public List<Output> stderr = new ArrayList<>();

    public void buildFromOriginal() {
        var t = originalResult.getThrowable();
        if (t != null) {
            var strWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(strWriter));
            stackTrace = strWriter.toString();
            exceptionClass = t.getClass().getName();
            exceptionMessage = t.getMessage();
        }
        name = originalResult.getName();

        switch (originalResult.getStatus()) {
            case ITestResult.SUCCESS:
                status = TestResultType.PASSED;
                break;
            case ITestResult.FAILURE:
                status = TestResultType.FAILED;
                break;
            case ITestResult.SKIP:
                status = TestResultType.SKIPPED;
                break;
        }

        if (name.matches("^\\w+ on \\w+\\(\\S+\\)")) {
            // remove instance string
            name = name.substring(0, name.indexOf(" "));
        }
        groupsDependedUpon = Arrays.asList(originalResult.getMethod().getGroupsDependedUpon());
        methodsDependedUpon = Arrays.asList(originalResult.getMethod().getMethodsDependedUpon());
        timeElapsed = originalResult.getEndMillis() - originalResult.getStartMillis();
    }

    @Override
    public String toString() {
        try {
            var reportMode = NORMAL;
            if (parentContext != null) {
                reportMode = parentContext.mode;
            }

            if (reportMode == MUTED || reportMode == ANONYMOUS) {
                return "";
            }
            var builder = new StringBuilder();
            switch (status) {
                case PASSED:
                    if (reportMode == VERBOSE || reportMode == MAXVERBOSE) {
                        builder.append(String.format("SUCCESS: %s\n\t%d msec%s, weight: %d unit%s\n",
                                name, timeElapsed, timeElapsed == 1 ? "" : "s",
                                weight, weight == 1 ? "" : "s"));
                        if (!"".equals(description)) {
                            builder.append(String.format("\tDescription: %s\n", description));
                        }
                    }
                    break;
                case FAILED:
                    builder.append(String.format("FAILURE: %s\n\t%d msec%s, weight: %d unit%s\n",
                            name, timeElapsed, timeElapsed == 1 ? "" : "s",
                            weight, weight == 1 ? "" : "s"));
                    if (!"".equals(description)) {
                        builder.append(String.format("\tDescription: %s\n", description));
                    }
                    builder.append(String.format("\tException type: %s\n", exceptionClass));
                    if ((printExceptionMessage || reportMode == VERBOSE || reportMode == MAXVERBOSE)
                            && exceptionMessage != null) {
                        builder.append(String.format("\tDetailed information:  %s\n", exceptionMessage));
                    }
                    if (printStackTrace || reportMode == MAXVERBOSE) {
                        builder.append(String.format("\tStack trace:  %s", stackTrace));
                    }
                    break;
                case SKIPPED:
                    builder.append(String.format("SKIPPED: %s\n\tWeight: %d unit%s\n",
                            name, weight, weight == 1 ? "" : "s"));
                    if (!"".equals(description)) {
                        builder.append(String.format("\tDescription: %s\n", description));
                    }
                    builder.append(String.format("\tTest skipped because:  %s\n", exceptionMessage));

                    if (groupsDependedUpon.size() > 0) {
                        builder.append(String.format("\tThis unit test depends on groups: %s\n",
                                String.join(", ", groupsDependedUpon)));
                    }
                    if (methodsDependedUpon.size() > 0) {
                        builder.append(String.format("\tThis unit test depends on tests: %s\n",
                                String.join(", ", methodsDependedUpon)));
                    }
                    break;
            }
            return builder.toString();
        } catch (NullPointerException e) {
            return super.toString() + " (incomplete object): " + e;
        }
    }

}
