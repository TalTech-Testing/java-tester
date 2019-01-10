package ee.ttu.java.studenttester.hodor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Input data for individual testers.
 */
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
