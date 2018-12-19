package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.helpers.StreamRedirector;
import ee.ttu.java.studenttester.core.model.tests.Output;
import ee.ttu.java.studenttester.core.model.tests.UnitTestContext;
import ee.ttu.java.studenttester.core.model.reports.CompilerReport;
import ee.ttu.java.studenttester.core.model.TesterContext;
import ee.ttu.java.studenttester.core.model.reports.TestNGReport;
import org.apache.commons.io.FileUtils;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

import static ee.ttu.java.studenttester.core.enums.CompilationResult.*;
import static ee.ttu.java.studenttester.core.helpers.AnnotationUtils.getClassMetadata;
import static ee.ttu.java.studenttester.core.helpers.AnnotationUtils.getMockTestContextConfiguration;
import static ee.ttu.java.studenttester.core.model.tests.Output.MAX_STREAM_READ_SIZE;

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

    private TestNGReport report = new TestNGReport();

    public TestNGRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws Exception {
        if (isFailed()) {
            LOG.severe("Unit testing can not be run as the compilation failed.");
            return;
        }

        TestNG testng = new TestNG();
        URLClassLoader loader = URLClassLoader.newInstance(new URL[] {context.tempRoot.toURI().toURL()});
        testng.addClassLoader(loader);

        var suite = new XmlSuite();
        var suites = List.of(suite);
        suite.setName(context.testRoot.getName());
        suite.setTimeOut(String.valueOf(timeOut));
        if (unitTestParams != null) {
            suite.setParameters(unitTestParams);
        }

        var testFiles = new ArrayList<>(FileUtils.listFiles(context.testRoot, JAVA_FILTER, true));
        for (var testFile : testFiles) {

            String testFileAsClassPath = ClassUtils.filePathToClassPath(testFile, context.testRoot);

            try {
                Class testClass = loader.loadClass(testFileAsClassPath);

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

        testng.setXmlSuites(suites);
        testng.setUseDefaultListeners(false);
        testng.addListener((ITestNGListener) report.resultListener);
        testng.setVerbose(0);
        StreamRedirector.enableNullStdin();
        StreamRedirector.beginRedirect();
        testng.run();
        report.status = testng.getStatus();
        report.incompleteGrade = ((CompilerReport) context.results.get(Identifier.COMPILER)).compilationResult != SUCCESS;
        parseResults();
    }

    @Override
    public void commit() {
        if (isFailed()) {
            return;
        }
        context.results.put(Identifier.TESTNG, report);
    }

    private boolean isFailed() {
        var compilationResult = ((CompilerReport) context.results.get(Identifier.COMPILER)).compilationResult;
        return compilationResult == FAILURE || compilationResult == NOT_RUN;
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
                conf = getMockTestContextConfiguration(conf.mode(), conf.welcomeMessage(), index);
            }

            context.originalContext = tc;
            context.configuration = conf;
            context.buildFromOriginal();

            report.testContexts.add(context);
            identifiers.add(index++);
        }

        report.testContexts.stream()
                .map(UnitTestContext::getAllResults)
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

        report.testContexts.sort(Comparator.comparing(c -> c.configuration.identifier()));
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
