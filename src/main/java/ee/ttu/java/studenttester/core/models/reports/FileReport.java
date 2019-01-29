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
        return Integer.MIN_VALUE;
    }

    public List<FileResource> files;

}
