package ee.ttu.java.studenttester.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Input data for individual testers.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TesterInput {

	/**
	 * Path to the directory containing submission files.
	 */
	@JsonProperty(required = true)
	public String contentRoot;

	/**
	 * Path to the directory containing test files.
	 */
	@JsonProperty(required = true)
	public String testRoot;

	/**
	 * Additional tester-specific information.
	 */
	public String extra;

}
