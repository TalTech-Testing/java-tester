package ee.ttu.java.studenttester.core.model.reports;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ttu.java.studenttester.core.interfaces.ReportElement;
import ee.ttu.java.studenttester.core.model.SerializableAuditEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckStyleReport implements ReportElement {

    @JsonProperty("count")
    public int checkStyleErrorCount = -1;

    @JsonProperty("errors")
    public Map<String, ArrayList<SerializableAuditEvent>> checkStyleResultMap;

    @JsonIgnore
    public File contentRoot;

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("* Checkstyle report *\n")
                .append("Found errors: ")
                .append(checkStyleErrorCount)
                .append("\n")
                .append(checkStyleResultMap.entrySet().stream()
                        .map(entry -> "File: "
                                + entry.getKey()
                                + "\n"
                                + entry.getValue().stream()
                                .map(Object::toString).collect(Collectors.joining("\n"))
                        ).collect(Collectors.joining("\n")))
                .append("\n\n");
        return builder.toString();
    }
}
