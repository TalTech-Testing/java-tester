package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.Parameter;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.JarReport;
import ee.ttu.java.studenttester.core.validators.FileOrDirectoryListParameterValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Runnable(identifier = Identifier.JAR, order = 4)
public class JarRunner extends BaseRunner {

	@Parameter(
			names = {"--additionalJars", "-jars"},
			description = "Comma-separated list of JAR archives or folders of JARs to load at runtime",
			order = 10,
			validateValueWith = FileOrDirectoryListParameterValidator.class

	)
	private List<File> jarFiles = new ArrayList<>();

	private ClassLoader jarEnhancedClassLoader;

	private JarReport report = new JarReport();

	public JarRunner(TesterContext context) {
		super(context);
	}

	@Override
	public void run() {
		File potentialLibFolder = new File(context.testRoot, "lib");
		if (potentialLibFolder.exists() && potentialLibFolder.isDirectory()) {
			LOG.info(String.format("Found directory %s, assuming it is for additional JARs", potentialLibFolder.getAbsolutePath()));
			jarFiles.add(potentialLibFolder);
		}
		jarFiles = jarFiles.stream()
				.flatMap(maybeDir -> {
					if (maybeDir.isDirectory()) {
						return FileUtils.listFiles(maybeDir, new String[] { "jar" }, true).stream();
					}
					return Stream.of(maybeDir);
				})
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(jarFiles)) {
			LOG.info("Found JARs to add to the classpath: " + jarFiles);
		} else {
			LOG.warning("Could not find any JARs to add to the classpath");
		}
		jarEnhancedClassLoader = URLClassLoader.newInstance(jarFiles.stream()
				.map(File::toURI)
				.map(uri -> {
					try {
						return uri.toURL();
					} catch (MalformedURLException e) {
						throw new StudentTesterException(e);
					}
				})
				.toArray(URL[]::new), getClass().getClassLoader());
	}

	@Override
	public void commit() {
		this.report.jarEnhancedClassLoader = jarEnhancedClassLoader;
		this.report.loadedJars = jarFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
		this.report.result = jarFiles.isEmpty() ? RunnerResultType.NOT_RUN : RunnerResultType.SUCCESS;
		this.context.results.putResult(report);
	}
}
