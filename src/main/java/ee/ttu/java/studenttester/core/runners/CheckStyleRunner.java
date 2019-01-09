package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.Parameter;
import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.interceptors.CheckStyleInterceptor;
import ee.ttu.java.studenttester.core.models.reports.CheckStyleReport;
import ee.ttu.java.studenttester.core.models.TesterContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Runnable(identifier = Identifier.CHECKSTYLE, order = 1)
public class CheckStyleRunner extends BaseRunner {

    private CheckStyleReport checkStyleReport = new CheckStyleReport();

    @Parameter(
            names = {"--checkstyleXml", "-csxml"},
            description = "Checkstyle XML rule file",
            order = 15
    )
    private String configFilePath = "/sun_checks.xml";

    public CheckStyleRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws StudentTesterException {
        try {
            var files = new ArrayList<>(FileUtils.listFiles(context.contentRoot, JAVA_FILTER, true));
            var listener = new CheckStyleInterceptor();
            listener.setBasePath(context.contentRoot);
            checkStyleReport.errors = listener.getErrors();
            checkStyleReport.checkStyleErrorCount = runCheckstyle(files, listener);
            checkStyleReport.result = RunnerResultType.SUCCESS;
        } catch (CheckstyleException e) {
            LOG.severe("Checkstyle testing has failed");
            e.printStackTrace();
        }
    }

    private int runCheckstyle(List<File> files, AuditListener auditListener) throws CheckstyleException {
        final var config = ConfigurationLoader.loadConfiguration(
                configFilePath,
                new PropertiesExpander(System.getProperties()),
                ConfigurationLoader.IgnoredModulesOptions.OMIT,
                new ThreadModeSettings(1, 1));

        int errorCounter;
        var moduleClassLoader = Checker.class.getClassLoader();
        var factory = new PackageObjectFactory(Checker.class.getPackage().getName(), moduleClassLoader);

        RootModule rootModule = (RootModule) factory.createModule(config.getName());
        rootModule.setModuleClassLoader(moduleClassLoader);
        rootModule.addListener(auditListener);
        rootModule.configure(config);

        try {
            errorCounter = rootModule.process(files);
        } catch (Exception e) {
            checkStyleReport.result = RunnerResultType.UNKNOWN;
            throw e;
        } finally {
            rootModule.destroy();
        }
        return errorCounter;
    }

    @Override
    public void commit() {
        context.results.putResult(checkStyleReport);
    }
}
