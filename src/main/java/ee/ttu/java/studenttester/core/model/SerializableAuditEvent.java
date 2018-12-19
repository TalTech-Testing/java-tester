package ee.ttu.java.studenttester.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

public class SerializableAuditEvent {

    @JsonIgnore
    private AuditEvent innerEvent;

    public String severityLevel;
    public int lineNo;
    public int columnNo;
    public String message;


    public SerializableAuditEvent(AuditEvent event) {
        this.innerEvent = event;
        this.severityLevel =  event.getSeverityLevel().toString().toUpperCase();
        this.lineNo = event.getLine();
        this.columnNo = event.getColumn();
        this.message = event.getMessage();
    }

    @Override
    public String toString() {
        return String.format("%s on line %d, col %s: %s", severityLevel, lineNo, columnNo, message);
    }
}
