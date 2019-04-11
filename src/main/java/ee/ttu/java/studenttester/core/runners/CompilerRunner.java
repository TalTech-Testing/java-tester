package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.SourceSetType;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.models.SerializableDiagnosticObject;
import ee.ttu.java.studenttester.core.models.reports.CompilerReport;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.JarReport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.tools.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Runnable(identifier = Identifier.COMPILER, order = 5)
public class CompilerRunner extends BaseRunner {

	private static final String MODULE_INFO_JAVA = "module-info.java";
	private static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private final String CLASSPATH_STR = System.getProperty("java.class.path");
    private final String MODULE_PATH_STR = System.getProperty("jdk.module.path");

    private CompilerReport compilerReport = new CompilerReport();

    @DynamicParameter(
            names = {"-O"},
            description = "Additional options to pass to the compiler. By default the source paths, module path and UTF-8 encoding are forced"
    )
    private Map<String, String> javacOpts;

    @Parameter(
            names = {"--separateCompilation", "-csep"},
            description = "Compile each unit test separately to skip ones that can't be compiled",
            arity = 1,
            order = 20
    )
    private boolean separateFileCompilation = true;

    @Parameter(
            names = {"--gradleLike", "-g"},
            description = "Signifies that source folders may be in Gradle or Maven-like format (src/main/java), " +
                    "implies --separateCompilation == false",
            arity = 1,
            order = 20
    )
    private boolean gradleLikeDirs = false;

    @Parameter(
            names = {"--discardModuleInfo", "-nomod"},
            description = "Discard module-info.java which may enable non-modular compilation",
            arity = 1,
            order = 20
    )
    private boolean discardModuleInfo = true;

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

	    if (MODULE_PATH_STR != null) {
		    javacOpts.put("--module-path", MODULE_PATH_STR);
	    }

        JarReport jars = context.results.getResultByType(JarReport.class);
        if (jars != null && CollectionUtils.isNotEmpty(jars.loadedJars)) {
            String classPathStr = CLASSPATH_STR;
            if (classPathStr != null && !classPathStr.isBlank()) {
                classPathStr = classPathStr + PATH_SEPARATOR;
            }
            javacOpts.put("-classpath", classPathStr + String.join(PATH_SEPARATOR, jars.loadedJars));
        }

        try {
            compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new StudentTesterException("The platform did not provide the necessary compiler needed"
                        + " to run this tool. Please check the availability of a JDK");
            }

            if (gradleLikeDirs || !separateFileCompilation) {
                separateFileCompilation = false;
                // feed files from multiple dirs and move compiled files to temp
                javacOpts.put("-d", context.tempRoot.getAbsolutePath());
            } else {
                // specify temp dir as source path, files must be prepared there
                javacOpts.put("-sourcepath", context.tempRoot.getAbsolutePath());
            }

            parseCopyDirs(context.contentRoot, context.tempRoot, compilerReport.codeFilesList, false, gradleLikeDirs);
            parseCopyDirs(context.testRoot, context.tempRoot, compilerReport.testFilesList, true, gradleLikeDirs);

            var testFiles = compilerReport.testFilesList;

            // exclude from compilation if overriden by test
            var testFilesRelative = testFiles.stream()
                    .map(file -> ClassUtils.relativizeFilePath(file, context.testRoot, compilerReport.testSourceType))
                    .collect(Collectors.toList());
            compilerReport.codeFilesList = compilerReport.codeFilesList.stream()
                    .filter(file -> !testFilesRelative.contains(ClassUtils.relativizeFilePath(file, context.contentRoot, compilerReport.codeSourceType)))
                    .collect(Collectors.toList());

            fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
            compilerWriter = new StringWriter();
            diagnosticCollector = new DiagnosticCollector<>();

            int successCount = 0;
            if (separateFileCompilation /* files already copied by parseCopyDirs() */) {
                for (var testFile : testFiles) {
                    var relative = ClassUtils.relativizeFilePath(testFile, context.testRoot, compilerReport.testSourceType);
                    var real = new File(context.tempRoot, relative);
                    if (compile(Collections.singletonList(real))) {
                        successCount++;
                    }
                }
            } else {
                // gradlelike or compiled together
                successCount = compile(Stream.concat(compilerReport.codeFilesList.stream(), testFiles.stream())
                        .collect(Collectors.toList())) ? 1 : 0;

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

    /**
     * Adjust & flatten & copy source code directories.
     */
    private void parseCopyDirs(File root, File dest, List<File> accumulator,
                               boolean test, boolean allowGradleLike) throws IOException {
        File hypotheticalSrcDir = new File(root, "src");
        boolean srcExists = hypotheticalSrcDir.exists() && hypotheticalSrcDir.isDirectory();

        File hypotheticalBuildGradle = new File(root, "build.gradle");
        boolean isGradleLike = hypotheticalBuildGradle.exists() && hypotheticalBuildGradle.isFile();

        File hypotheticalPomXml = new File(root, "pom.xml");
        boolean isMavenLike = hypotheticalPomXml.exists() && hypotheticalPomXml.isFile();

        if ((isGradleLike || isMavenLike) && allowGradleLike) {
            LOG.warning(String.format("Adding Gradle-like %s sources: %s", (test ? "test" : "code"), root));

            accumulator.addAll(getFromDir(new File(root, "src/main/java")));
            accumulator.addAll(getFromDir(new File(root, "src/test/java")));
            copyIfSrcExists(new File(root, "src/main/resources"), dest);
            copyIfSrcExists(new File(root, "src/test/resources"), dest);

            if (test) {
                compilerReport.testSourceType = SourceSetType.SRC_MAIN_JAVA;
            } else {
                compilerReport.codeSourceType = SourceSetType.SRC_MAIN_JAVA;
            }
        } else if (srcExists) {
            LOG.warning(String.format("Getting %s files from nested src: %s", (test ? "test" : "code"), hypotheticalSrcDir.getAbsolutePath()));
            accumulator.addAll(getFromDir(hypotheticalSrcDir));
            FileUtils.copyDirectory(hypotheticalSrcDir, dest);

            if (test) {
                compilerReport.testSourceType = SourceSetType.SRC;
            } else {
                compilerReport.codeSourceType = SourceSetType.SRC;
            }
        } else {
            LOG.warning(String.format("Getting %s files from: %s", (test ? "test" : "code"), root));
            accumulator.addAll(getFromDir(root));
            FileUtils.copyDirectory(root, dest);
        }

	    if (discardModuleInfo) {
		    var files = FileUtils.listFiles(context.tempRoot, FileFilterUtils.nameFileFilter(MODULE_INFO_JAVA), DirectoryFileFilter.INSTANCE);
		    files.forEach(file -> {
			    LOG.info("Renaming file " + file);
			    if (!file.renameTo(new File(file.getAbsolutePath() + ".txt"))) {
				    LOG.warning("Unable to rename file " + file);
			    }
		    });

		    accumulator.removeIf(f -> f.getName().equalsIgnoreCase(MODULE_INFO_JAVA));
	    }
    }

    private static List<File> getFromDir(File sourceDir) {
        if (sourceDir.isDirectory()) {
            return new ArrayList<>(FileUtils.listFiles(sourceDir, JAVA_FILTER, true));
        }
        return List.of();
    }

    private static void copyIfSrcExists(File sourceDir, File destDir) throws IOException {
        if (sourceDir.isDirectory()) {
            FileUtils.copyDirectory(sourceDir, destDir);
        }
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
