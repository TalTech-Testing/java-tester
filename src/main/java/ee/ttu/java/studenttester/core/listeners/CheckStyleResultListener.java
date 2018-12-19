package ee.ttu.java.studenttester.core.listeners;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;
import ee.ttu.java.studenttester.core.model.SerializableAuditEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class CheckStyleResultListener implements AuditListener {

    public CheckStyleResultListener() {
        this.fileResultErrorMap = new TreeMap<>();
    }

    private Map<String, ArrayList<SerializableAuditEvent>> fileResultErrorMap;

    @Override
    public void auditStarted(AuditEvent event) {
    }

    @Override
    public void auditFinished(AuditEvent event) {
    }

    @Override
    public void fileStarted(AuditEvent event) {
        fileResultErrorMap.put(event.getFileName(), new ArrayList<>());
    }

    @Override
    public void fileFinished(AuditEvent event) {
    }

    @Override
    public void addError(AuditEvent event) {
        fileResultErrorMap.get(event.getFileName()).add(new SerializableAuditEvent(event));
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        throw new RuntimeException(throwable);
    }

    public Map<String, ArrayList<SerializableAuditEvent>> getErrorMap() {
        return fileResultErrorMap;
    }

    public void relativizePaths(File path) {
        var filePaths = new HashSet<>(fileResultErrorMap.keySet());
        for (String fullFilePath : filePaths) {
            var tmp = fileResultErrorMap.remove(fullFilePath);
            var newPath = ClassUtils.relativizeFile(new File(fullFilePath), path);
            fileResultErrorMap.put(newPath, tmp);
        }
    }
}
