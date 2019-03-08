package ee.ttu.java.studenttester.core.models.reports;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.ttu.java.studenttester.core.annotations.Identifier;

import java.net.URL;
import java.util.List;

public class JarReport extends AbstractReport {

	@Override
	public Identifier getIdentifier() {
		return Identifier.JAR;
	}

	@Override
	public int getCode() {
		return 123;
	}

	@JsonIgnore
	public ClassLoader jarEnhancedClassLoader;

	public List<String> loadedJars;

	@Override
	public String toString() {
		return ""; // should not be visible in the report
	}

}
