package ee.ttu.java.studenttester.core.models.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.models.SerializableAuditEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CheckStyleReport extends AbstractReport {

    @Override
    public Identifier getIdentifier() {
        return Identifier.CHECKSTYLE;
    }

    @Override
    public int getCode() {
        return 101;
    }

    @JsonProperty("count")
    public int checkStyleErrorCount = -1;

    @JsonProperty("errors")
    public List<SerializableAuditEvent> errors;

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("* Checkstyle report *\n\n")
                .append("Found errors: ");
        if (result != RunnerResultType.SUCCESS) {
            builder.append("? (stylecheck failed unexpectedly)");
        } else {
            builder.append(checkStyleErrorCount);
        }
        builder.append("\n");
        var groups = errors.stream().collect(Collectors.groupingBy(a -> a.fileName));
        for (var group : groups.values()) {
            builder.append(group.get(0)).append('\n');
            group.stream()
                    .skip(1)
                    .forEach(a -> builder.append(a.toStringAsNext()).append('\n'));
        }
        return builder.toString();
    }

}
