package ee.ttu.java.studenttester.core;

import com.google.common.io.Files;
import ee.ttu.java.studenttester.core.model.reports.CompilerReport;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.CompilationResult;
import ee.ttu.java.studenttester.core.model.TesterContext;
import ee.ttu.java.studenttester.core.runners.CompilerRunner;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

import java.io.File;
import java.io.IOException;

public abstract class BaseTest {

    protected TesterContext context;

    @AfterMethod
    public void cleanUp() throws Exception {
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

    protected void compileAndExpect(CompilationResult expected) {
        var compiler = new CompilerRunner(context);
        context.runners.put(Identifier.COMPILER, compiler);
        compiler.setCopyResources(false);
        compiler.run();
        compiler.commit();
        Assert.assertEquals(((CompilerReport) context.results.get(Identifier.COMPILER)).compilationResult, expected);
    }

    protected void moveResource(String resource, File destination) throws IOException {
        FileUtils.copyFileToDirectory(getFile(resource), destination);
    }

    private File getFile(String resource) {
        return new File(getClass().getResource(resource).getPath());
    }
}
