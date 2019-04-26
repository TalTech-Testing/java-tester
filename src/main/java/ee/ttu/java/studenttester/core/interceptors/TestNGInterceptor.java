package ee.ttu.java.studenttester.core.interceptors;

import ee.ttu.java.studenttester.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.StderrStreamMap;
import ee.ttu.java.studenttester.core.helpers.StdoutStreamMap;
import ee.ttu.java.studenttester.core.helpers.StreamRedirector;
import ee.ttu.java.studenttester.core.security.RogueThreadHandler;
import ee.ttu.java.studenttester.core.security.SecureEnvironment;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.collections.Pair;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static ee.ttu.java.studenttester.core.helpers.AnnotationUtils.getClassMetadata;

public class TestNGInterceptor implements ITestListener {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    private SecureEnvironment secEnv = SecureEnvironment.getInstance();
    private List<ITestContext> testContexts = new ArrayList<>();
    private Map<ITestResult, Pair<StdoutStreamMap, StderrStreamMap>> testStreams = new LinkedHashMap<>();

    private RogueThreadHandler threadHandler;

    private void handleTestEnd(ITestResult result) {
        threadHandler.checkHandleRogueThreads();
        if (RogueThreadHandler.getRogueThreads().isEmpty()) {
            secEnv.disableCustomSecurityManager();
        }
        testStreams.put(result,
                new Pair<>(StreamRedirector.getStdoutStreams(), StreamRedirector.getStderrStreams()));
        StreamRedirector.reset();
    }

    @Override
    public void onStart(ITestContext context) {
        testContexts.add(context);
        threadHandler = new RogueThreadHandler();
        secEnv.enableCustomSecurityManager();
        secEnv.setDefaultRestrictions();
        TestContextConfiguration classMetadata = getClassMetadata(context);
        if (!Collections.disjoint(Arrays.asList(classMetadata.disablePolicies()), Arrays.asList(classMetadata.enablePolicies()))) {
            throw new StudentTesterException("Cannot enable and disable a policy at the same time!");
        }
        Stream.of(classMetadata.disablePolicies())
                .forEach(secEnv::removePolicy);
        Stream.of(classMetadata.enablePolicies())
                .forEach(secEnv::addPolicy);
        LOG.info(String.format("Starting test context %s, enabled policies %s", context.getName(), secEnv.getCurrentPolicies()));
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info(String.format("Context %s finished in %d ms",
                context.getName(), context.getEndDate().getTime() - context.getStartDate().getTime()));
    }

    @Override
    public void onTestStart(ITestResult result) {
        StreamRedirector.enableNullStdin();
        StreamRedirector.beginRedirect();
        secEnv.enableCustomSecurityManager();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestEnd(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        handleTestEnd(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        handleTestEnd(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        handleTestEnd(result);
    }

    public List<ITestContext> getTestContexts() {
        return testContexts;
    }

    public Map<ITestResult, Pair<StdoutStreamMap, StderrStreamMap>> getTestStreams() {
        return testStreams;
    }

}
