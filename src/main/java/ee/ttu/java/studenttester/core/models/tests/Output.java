package ee.ttu.java.studenttester.core.models.tests;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Output {

    @JsonIgnore
    public static final int MAX_STREAM_READ_SIZE = 8192;

    public String thread;
    public boolean truncated;
    public String content;

}
