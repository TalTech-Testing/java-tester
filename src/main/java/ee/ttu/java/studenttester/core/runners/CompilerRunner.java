package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.models.SerializableDiagnosticObject;
import ee.ttu.java.studenttester.core.models.reports.CompilerReport;
import ee.ttu.java.studenttester.core.models.TesterContext;
import org.apache.commons.io.FileUtils;

import javax.tools.*;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Runnable(identifier = Identifier.COMPILER, order = 5)
public class CompilerRunner extends BaseRunner {

    private CompilerReport compilerReport = new CompilerReport();

    @DynamicParameter(
            names = {"-O"},
            description = "Additional options to pass to the compiler. By default the source path and UTF-8 encoding are forced"
    )
    private Map<String, String> javacOpts;

    @Parameter(
            names = {"--separateCompilation", "-csep"},
            description = "Compile each unit test separately to skip ones that can't be compiled",
            arity = 1,
            order = 20
    )
    private boolean separateFileCompilation = true;

    private boolean copyResources = true;
    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private Writer compilerWriter;
    private DiagnosticCollector<JavaFileObject> diagnosticCollector;

    public CompilerRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws StudentTesterException {
        if (javacOpts == null) {
            javacOpts = new HashMap<>();
        }
        javacOpts.put("-encoding", "utf8");
        javacOpts.put("-sourcepath", context.tempRoot.getAbsolutePath());

        try {
            compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new StudentTesterException("The platform did not provide the necessary compiler needed"
                        + " to run this tool. Please check the availability of a JDK");
            }

            FileUtils.copyDirectory(context.contentRoot, context.tempRoot);
            FileUtils.copyDirectory(context.testRoot, context.tempRoot);
            var testFiles = new ArrayList<>(FileUtils.listFiles(context.testRoot, JAVA_FILTER, true));

            fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
            compilerWriter = new StringWriter();
            diagnosticCollector = new DiagnosticCollector<>();

            int successCount = 0;
            if (separateFileCompilation) {
                for (var testFile : testFiles) {
                    var relative = ClassUtils.relativizeFilePath(testFile, context.testRoot);
                    var real = new File(context.tempRoot, relative);
                    if (compile(Collections.singletonList(real))) {
                        successCount++;
                    }
                }
            } else {
                successCount = compile(testFiles) ? 1 : 0;
            }

            fileManager.close();

            if (successCount > 0 && successCount < testFiles.size() && separateFileCompilation) {
                // separate succeeded less than total
                compilerReport.result = RunnerResultType.PARTIAL_SUCCESS;
                LOG.warning("There was at least one compilation failure");
            } else if (successCount != 0 && successCount == testFiles.size()
                    || successCount == 1 && !separateFileCompilation) {
                // succeeded equals total OR 1 success and no separation
                compilerReport.result = RunnerResultType.SUCCESS;
            } else if (testFiles.size() == 0) {
                compilerReport.result = RunnerResultType.NOT_RUN;
                LOG.warning("Nothing to compile");
            } else {
                compilerReport.result = RunnerResultType.FAILURE;
                LOG.severe("Compilation has failed");
            }

            compilerReport.diagnosticList = diagnosticCollector.getDiagnostics().stream()
                    .map(diagnostic -> new SerializableDiagnosticObject(diagnostic, context))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.severe("An unexpected compilation error has occurred");
            compilerReport.result = RunnerResultType.FAILURE;
            e.printStackTrace();
        }
    }

    private boolean compile(final List<File> filenames) {
        var compilationUnits = fileManager.getJavaFileObjectsFromFiles(filenames);
        var opts = javacOpts.entrySet().stream()
                .flatMap(opt -> Stream.of(opt.getKey(), opt.getValue()))
                .collect(Collectors.toList());
        return compiler.getTask(compilerWriter, null, diagnosticCollector, opts, null, compilationUnits).call();
    }

    @Override
    public void commit() {
        context.results.putResult(compilerReport);
    }

    public boolean isCopyResources() {
        return copyResources;
    }

    public void setCopyResources(boolean copyResources) {
        this.copyResources = copyResources;
    }
}
