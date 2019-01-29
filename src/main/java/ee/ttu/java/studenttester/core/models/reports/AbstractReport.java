package ee.ttu.java.studenttester.core.models.reports;

import com.fasterxml.jackson.annotation.*;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@JsonTypeInfo(include = JsonTypeInfo.As.EXISTING_PROPERTY, use = JsonTypeInfo.Id.NAME, property = "identifier")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CheckStyleReport.class, name = "CHECKSTYLE"),
        @JsonSubTypes.Type(value = FileReport.class, name = "FILEWRITER"),
        @JsonSubTypes.Type(value = CompilerReport.class, name = "COMPILER"),
        @JsonSubTypes.Type(value = TestNGReport.class, name = "TESTNG"),
        @JsonSubTypes.Type(value = PlainTextReport.class, name = "REPORT"),
})
@JsonPropertyOrder(alphabetic = true)
public abstract class AbstractReport {
    /**
     * Enum representation for this report type. Assumed to be static.
     * @return enum associated with this report type
     */
    @JsonProperty(access = READ_ONLY)
    public abstract Identifier getIdentifier();

    /**
     * Unique numeric identifier for this report type. Assumed to be static.
     * @return code associated with this report type.
     */
    @JsonProperty(access = READ_ONLY)
    public abstract int getCode();

    public RunnerResultType result = RunnerResultType.NOT_SET;
}
