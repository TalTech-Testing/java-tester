package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.models.FileResource;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.FileReport;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@Runnable(identifier = Identifier.FILEWRITER, order = 2)
public class FileWriterRunner extends BaseRunner {

    public FileWriterRunner(TesterContext context) {
        super(context);
    }

    private FileReport fileReport = new FileReport();

    @Override
    public void run() throws Exception {
        fileReport.files = new ArrayList<>();
        fileReport.result = RunnerResultType.SUCCESS;
        FileUtils.listFiles(context.testRoot, JAVA_FILTER, true).stream()
                .map(f -> fileToResource(f, true))
                .filter(Objects::nonNull)
                .forEach(fileReport.files::add);
        FileUtils.listFiles(context.contentRoot, JAVA_FILTER, true).stream()
                .map(f -> fileToResource(f, false))
                .filter(Objects::nonNull)
                .forEach(fileReport.files::add);
    }

    @Override
    public void commit() {
        context.results.putResult(fileReport);
    }

    private FileResource fileToResource(File file, boolean isTest) {
        var fr = new FileResource();
        fr.path = ClassUtils.relativizeFilePath(file, isTest ? context.testRoot : context.contentRoot);
        fr.isTest = isTest;
        try {
            fr.contents = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            LOG.severe(String.format("Unable to read file %s:", fr.path));
            e.printStackTrace();
        }
        return fr;
    }
}
