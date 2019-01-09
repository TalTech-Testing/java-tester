package ee.ttu.java.studenttester.core.enums;

public enum RunnerResultType {

    /**
     * The specified runner explicitly states it did not run.
     */
    NOT_RUN,

    /**
     * The specified runner has failed.
     */
    FAILURE,

    /**
     * The specified runner ran but there were failures of some sort.
     */
    PARTIAL_SUCCESS,

    /**
     * The specified runner ran without problems.
     */
    SUCCESS,

    /**
     * The specified runner ran but it's unclear what the result is.
     */
    UNKNOWN,

    /**
     * The programmer was lazy and forgot to update the status.
     */
    NOT_SET

}
