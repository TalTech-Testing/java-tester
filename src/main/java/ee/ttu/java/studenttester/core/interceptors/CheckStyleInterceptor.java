package ee.ttu.java.studenttester.core.interceptors;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.models.SerializableAuditEvent;

import java.io.File;
import java.util.*;

/**
 * Custom listener for getting all errors. The same listener must not be used with multiple threads.
 */
public class CheckStyleInterceptor implements AuditListener {

    private List<SerializableAuditEvent> errorList = new ArrayList<>();

    private File basePath;
    private String currentFile;

    @Override
    public void auditStarted(AuditEvent event) {
    }

    @Override
    public void auditFinished(AuditEvent event) {
    }

    @Override
    public void fileStarted(AuditEvent event) {
        if (basePath != null) {
            currentFile = ClassUtils.relativizeFilePath(new File(event.getFileName()), basePath);
        } else {
            basePath = new File(".");
        }
    }

    @Override
    public void fileFinished(AuditEvent event) {
    }

    @Override
    public void addError(AuditEvent event) {
        errorList.add(new SerializableAuditEvent(event, currentFile));
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {

        throw new RuntimeException(throwable);
    }

    public List<SerializableAuditEvent> getErrors() {
        return errorList;
    }

    public void setBasePath(File path) {
        this.basePath = path;
    }
}
