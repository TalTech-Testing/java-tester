package ee.ttu.java.studenttester.core;

import com.google.common.io.Files;
import ee.ttu.java.studenttester.core.models.reports.CompilerReport;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.JarReport;
import ee.ttu.java.studenttester.core.runners.CompilerRunner;
import ee.ttu.java.studenttester.core.runners.TestNGRunner;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

public abstract class BaseTest {

    protected static final int HAS_SKIPPED = 2;
    protected static final int CONTAINS_FAILED = 1;
    protected static final int SUCCESS = 0; // but not necessarily complete success, as the compiler might have failed

    protected static final int TIMEOUT = 5000;

    protected TesterContext context;

    @AfterMethod
    public void cleanUp() throws Exception {
        JarReport jarReport = context.results.getResultByType(JarReport.class);
        if (jarReport != null && jarReport.jarEnhancedClassLoader != null) {
            ((URLClassLoader) jarReport.jarEnhancedClassLoader).close(); // release jar files
        }
        if (context.testRoot != null) {
            FileUtils.deleteDirectory(context.testRoot);
        }
        if (context.contentRoot != null) {
            FileUtils.deleteDirectory(context.contentRoot);
        }
        context.close();
    }

    protected void initContext() {
        context = new TesterContext();
        context.testRoot = Files.createTempDir();
        context.contentRoot = Files.createTempDir();
    }

    protected void compileAndExpect(RunnerResultType expected) {
        var compiler = new CompilerRunner(context);
        context.runners.put(Identifier.COMPILER, compiler);
        compiler.setCopyResources(false);
        compiler.run();
        compiler.commit();
        Assert.assertEquals(context.results.getResultByType(CompilerReport.class).result, expected,
                context.results.getResultByType(CompilerReport.class).diagnosticList.toString());
    }

    protected void moveResource(String resource, File destination) throws IOException {
        FileUtils.copyFileToDirectory(getFile(resource), destination);
    }

    private File getFile(String resource) {
        return new File(getClass().getResource(resource).getPath());
    }

    protected void runNewTestNG() throws Exception {
        var runner = new TestNGRunner(context);
        runner.setTimeOut(TIMEOUT);
        runner.run();
        runner.commit();
    }
}
