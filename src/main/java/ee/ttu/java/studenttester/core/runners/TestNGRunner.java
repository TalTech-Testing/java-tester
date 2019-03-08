package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.TesterPolicy;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.helpers.StreamRedirector;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.CompilerReport;
import ee.ttu.java.studenttester.core.models.reports.JarReport;
import ee.ttu.java.studenttester.core.models.reports.TestNGReport;
import ee.ttu.java.studenttester.core.models.tests.Output;
import ee.ttu.java.studenttester.core.models.tests.UnitTestContext;
import ee.ttu.java.studenttester.core.security.SecureEnvironment;
import org.apache.commons.io.FileUtils;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ee.ttu.java.studenttester.core.enums.RunnerResultType.NOT_RUN;
import static ee.ttu.java.studenttester.core.enums.RunnerResultType.PARTIAL_SUCCESS;
import static ee.ttu.java.studenttester.core.enums.RunnerResultType.SUCCESS;
import static ee.ttu.java.studenttester.core.helpers.AnnotationUtils.getClassMetadata;
import static ee.ttu.java.studenttester.core.helpers.AnnotationUtils.getMockTestContextConfiguration;
import static ee.ttu.java.studenttester.core.models.tests.Output.MAX_STREAM_READ_SIZE;

@Runnable(identifier = Identifier.TESTNG, order = 10)
public class TestNGRunner extends BaseRunner {

    @DynamicParameter(
            names = {"-U"},
            description = "Parameters to pass to unit tests. Refer to TestNG's @Parameters documentation"
    )
    private Map<String, String> unitTestParams;

    @Parameter(
            names = {"--timeout", "-to"},
            description = "Maximum number of milliseconds each unit test can run before being terminated"
    )
    private int timeOut = 15_000;

    private SecureEnvironment secEnv = SecureEnvironment.getInstance();
    private TestNGReport report = new TestNGReport();

    public TestNGRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws Exception {
        if (!canContinue()) {
            LOG.severe("Unit testing can not be run as the compilation failed.");
            report.result = NOT_RUN;
            return;
        }

        TestNG testng = new TestNG();
        JarReport jars = context.results.getResultByType(JarReport.class);
        if (jars != null && jars.jarEnhancedClassLoader != null) {
            getTempClassLoader(jars.jarEnhancedClassLoader, true);
        } else {
            getTempClassLoader(null, true);
        }
        testng.addClassLoader(getTempClassLoader(null, false));

        var suite = new XmlSuite();
        var suites = List.of(suite);
        suite.setName(context.testRoot.getName());
        suite.setTimeOut(String.valueOf(timeOut));
        if (unitTestParams != null) {
            suite.setParameters(unitTestParams);
        }

        var testFiles = new ArrayList<>(FileUtils.listFiles(context.testRoot, JAVA_FILTER, true));
        for (var testFile : testFiles) {

            secEnv.addProtectedFile(Paths.get(testFile.toURI())); // add .java file to protected list
            String relativeName = ClassUtils.relativizeFilePath(testFile, context.testRoot);
            File copiedFile = new File(context.tempRoot, relativeName);

            secEnv.addProtectedFile(Paths.get(copiedFile.toURI())); // add .java file to protected list in temp folder
            secEnv.addProtectedFile(Paths.get(copiedFile.getAbsolutePath().replace(".java", ".class"))); // add .class file to protected list

            String testFileAsClassPath = ClassUtils.filePathToClassPath(testFile, context.testRoot);

            try {
                Class testClass = getTempClassLoader(null, false).loadClass(testFileAsClassPath);

                XmlTest test;
                List<XmlClass> classes;

                switch (ClassUtils.getClassType(testClass)) {
                    case JUNIT:
                        test = new XmlTest(suite);
                        classes = List.of(new XmlClass(testClass));
                        test.setXmlClasses(classes);
                        test.setName(ClassUtils.filePathToClassPath(testFile, context.testRoot) + " (JUnit)");
                        test.setJunit(true);
                        break;
                    case TESTNG:
                        test = new XmlTest(suite);
                        classes = List.of(new XmlClass(testClass));
                        test.setXmlClasses(classes);
                        test.setName(ClassUtils.filePathToClassPath(testFile, context.testRoot) + " (TestNG)");
                        break;
                    case MIXED:
                        LOG.warning(String.format("Skipping class %s due to mixed usage of test annotations", testFileAsClassPath));
                    default:
                        break;
                }
            } catch (ClassNotFoundException e) {
                LOG.warning("Skipping possibly uncompiled class " + testFileAsClassPath);
            }
        }

        var codeFiles = new ArrayList<>(FileUtils.listFiles(context.contentRoot, JAVA_FILTER, true));
        for (var codeFile : codeFiles) {
            String codeFileAsClassPath = ClassUtils.filePathToClassPath(codeFile, context.contentRoot);
            try {
                Class unsafeClass = getTempClassLoader(null, false).loadClass(codeFileAsClassPath);
                secEnv.addClass(unsafeClass);
            } catch (ClassNotFoundException e) {
                LOG.warning("Skipping possibly uncompiled class " + codeFileAsClassPath);
            }
        }

        testng.setXmlSuites(suites);
        testng.setUseDefaultListeners(false);
        testng.addListener((ITestNGListener) report.resultListener);
        testng.setVerbose(0);
        StreamRedirector.enableNullStdin();
        StreamRedirector.beginRedirect();
        secEnv.setDefaultRestrictions();
        secEnv.addPolicy(TesterPolicy.DISABLE_SOCKETS);
        LOG.info("Beginning testing, enabled policies: " + secEnv.getCurrentPolicies());
        try {
            testng.run();
        } finally {
            StreamRedirector.reset();
            report.securityViolation = secEnv.isTriggered();
            secEnv.resetAll();
        }
        report.testNGStatus = testng.getStatus();
        if (context.results.getResultByType(CompilerReport.class).result != SUCCESS) {
            report.result = PARTIAL_SUCCESS;
        } else {
            report.result = SUCCESS;
        }
        parseResults();
    }

    @Override
    public void commit() {
        context.results.putResult(report);
    }

    private boolean canContinue() {
        var compilationResult = context.results.getResultByType(CompilerReport.class).result;
        return compilationResult == SUCCESS || compilationResult == PARTIAL_SUCCESS;
    }

    private void parseResults() {
        var testContexts = report.resultListener.getTestContexts();
        var testStreams = report.resultListener.getTestStreams();
        var identifiers = new ArrayList<Integer>();
        int index = 0;

        for (var tc : testContexts) {
            UnitTestContext context = new UnitTestContext();

            var conf = getClassMetadata(tc);

            if (conf.identifier() > -1) { // if identifier is found, use this instead
                if (identifiers.contains(conf.identifier())) {
                    throw new StudentTesterException(tc.getCurrentXmlTest().getClasses().get(0).getName()
                            + " clashes with already existing identifier " + conf.identifier());
                }
                index = conf.identifier();
            } else {
                conf = getMockTestContextConfiguration(conf.mode(), conf.welcomeMessage(), index + 1);
            }

            context.originalContext = tc;

            context.identifier = conf.identifier();
            context.mode = conf.mode();
            context.welcomeMessage = conf.welcomeMessage();

            context.buildFromOriginal();

            report.testContexts.add(context);
            identifiers.add(index++);
        }

        report.testContexts.stream()
                .map(c -> c.unitTests)
                .flatMap(List::stream)
                .forEach(singleResult -> {
                    var streams = testStreams.get(singleResult.originalResult);
                    if (streams != null) {
                        var stdoutsByThread = streams.first();
                        var stderrsByThread = streams.second();
                        if (stdoutsByThread.size() > 1 || stderrsByThread.size() > 1) {
                            LOG.warning(String.format("There were multiple threads running concurrently," +
                                    " outputs may be inconsistent: stdout threads: %d, stderr threads: %d",
                                    stdoutsByThread.size(), stderrsByThread.size()));
                        }
                        singleResult.stdout = stdoutsByThread.entrySet().stream()
                                .map(entry -> getOutput(entry.getKey().getName(), entry.getValue()))
                                .collect(Collectors.toList());

                        singleResult.stderr = stderrsByThread.entrySet().stream()
                                .map(entry -> getOutput(entry.getKey().getName(), entry.getValue()))
                                .collect(Collectors.toList());
                    }
                });

        report.testContexts.sort(Comparator.comparing(c -> c.identifier));
    }

    private static Output getOutput(String threadName, ByteArrayOutputStream baos) {
        var output = new Output();
        output.thread = threadName;

        byte[] bytes = baos.toByteArray();
        if (baos.size() > MAX_STREAM_READ_SIZE) {
            output.truncated = true;
            bytes = Arrays.copyOf(bytes, MAX_STREAM_READ_SIZE);
        }

        output.content = new String(bytes);
        return output;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
}
