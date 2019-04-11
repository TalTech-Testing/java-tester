package ee.ttu.java.studenttester.core.models.reports;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.enums.SourceSetType;
import ee.ttu.java.studenttester.core.models.SerializableDiagnosticObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompilerReport extends AbstractReport {

    @Override
    public Identifier getIdentifier() {
        return Identifier.COMPILER;
    }

    @Override
    public int getCode() {
        return 102;
    }

    public List<SerializableDiagnosticObject> diagnosticList;

    // all files eligible for compilation
    @JsonIgnore
    public List<File> codeFilesList = new ArrayList<>();

    @JsonIgnore
    public List<File> testFilesList = new ArrayList<>();

    @JsonIgnore
    public SourceSetType codeSourceType = SourceSetType.ROOT;

    @JsonIgnore
    public SourceSetType testSourceType = SourceSetType.ROOT;


    @Override
    public String toString() {
        var builder = new StringBuilder()
            .append("* Compiler report *\n\n");

        switch (result) {
            case NOT_RUN:
                builder.append("The compiler did not run. Were there any files to compile?");
                break;
            case FAILURE:
                builder.append("Compilation failed.");
                break;
            case PARTIAL_SUCCESS:
                builder.append("Compilation succeeded partially.");
                break;
            case SUCCESS:
                builder.append("Compilation succeeded.");
        }

        if (diagnosticList != null && !diagnosticList.isEmpty()) {
            builder.append("\nDiagnostic information:\n");
            diagnosticList.forEach(builder::append);
        }

        return builder.toString();
    }
}
