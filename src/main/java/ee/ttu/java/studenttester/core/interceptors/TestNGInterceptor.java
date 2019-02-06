package ee.ttu.java.studenttester.core.interceptors;

import ee.ttu.java.studenttester.core.helpers.StderrStreamMap;
import ee.ttu.java.studenttester.core.helpers.StdoutStreamMap;
import ee.ttu.java.studenttester.core.helpers.StreamRedirector;
import ee.ttu.java.studenttester.core.security.SecureEnvironment;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.collections.Pair;
import org.testng.internal.thread.ThreadUtil;

import java.util.*;
import java.util.logging.Logger;

public class TestNGInterceptor implements ITestListener {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    private SecureEnvironment secEnv = SecureEnvironment.getInstance();
    private List<ITestContext> testContexts = new ArrayList<>();
    private Map<ITestResult, Pair<StdoutStreamMap, StderrStreamMap>> testStreams = new LinkedHashMap<>();

    public List<ITestContext> getTestContexts() {
        return testContexts;
    }

    public Map<ITestResult, Pair<StdoutStreamMap, StderrStreamMap>> getTestStreams() {
        return testStreams;
    }

    private void handleTestEnd(ITestResult result) {
        secEnv.disableCustomSecurityManager();
        testStreams.put(result,
                new Pair<>(StreamRedirector.getStdoutStreams(), StreamRedirector.getStderrStreams()));
        StreamRedirector.reset();

        Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith(ThreadUtil.THREAD_NAME))
                .forEach(thread -> {
                    LOG.warning(String.format("Found thread %s that seems to be stuck. " +
                            "Trying to kill and hoping for the best...", thread.getName()));
                    thread.stop();
                });
    }

    @Override
    public void onStart(ITestContext context) {
        testContexts.add(context);
        secEnv.enableCustomSecurityManager();
        LOG.info(String.format("Starting test context %s", context.getName()));
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


}
