package ee.ttu.java.studenttester.core.models.reports;

import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.models.FileResource;

import java.util.List;

public class FileReport extends AbstractReport {

    @Override
    public Identifier getIdentifier() {
        return Identifier.FILEWRITER;
    }

    @Override
    public int getCode() {
        return 103;
    }

    public List<FileResource> files;

    @Override
    public String toString() {
        return ""; // should not be visible in the report
    }
}
