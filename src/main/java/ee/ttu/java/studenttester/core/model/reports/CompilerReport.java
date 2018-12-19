package ee.ttu.java.studenttester.core.model.reports;

import ee.ttu.java.studenttester.core.interfaces.ReportElement;
import ee.ttu.java.studenttester.core.model.SerializableDiagnosticObject;
import ee.ttu.java.studenttester.core.enums.CompilationResult;

import java.util.List;

public class CompilerReport implements ReportElement {

    public CompilationResult compilationResult = CompilationResult.NOT_RUN;

    public List<SerializableDiagnosticObject> diagnosticList;

    @Override
    public String toString() {
        var builder = new StringBuilder()
            .append("* Compiler report *\n");

        switch (compilationResult) {
            case NOT_RUN:
                builder.append("The compiler did not run.");
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

        return builder.append('\n').toString();
    }
}
