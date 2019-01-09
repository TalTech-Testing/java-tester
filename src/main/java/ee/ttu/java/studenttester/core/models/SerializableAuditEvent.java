package ee.ttu.java.studenttester.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

public class SerializableAuditEvent {

    @JsonIgnore
    private AuditEvent innerEvent;

    public String fileName;
    public String severityLevel;
    public int lineNo;
    public int columnNo;
    public String message;


    public SerializableAuditEvent(AuditEvent event) {
        this(event, event.getFileName());
    }

    /**
     * Constructor for overriding the default file name.
     * @param event Checkstyle event
     * @param fileName custom file name
     */
    public SerializableAuditEvent(AuditEvent event, String fileName) {
        this.innerEvent = event;
        this.fileName = fileName;
        this.severityLevel =  event.getSeverityLevel().toString().toUpperCase();
        this.lineNo = event.getLine();
        this.columnNo = event.getColumn();
        this.message = event.getMessage();
    }

    @Override
    public String toString() {
        return String.format("%s in %s on line %d, col %s: %s", severityLevel, fileName, lineNo, columnNo, message);
    }

    public String toStringAsNext() {
        return String.format("\t+%s on line %d, col %s: %s", severityLevel, lineNo, columnNo, message);
    }

    public SerializableAuditEvent() {

    }
}
