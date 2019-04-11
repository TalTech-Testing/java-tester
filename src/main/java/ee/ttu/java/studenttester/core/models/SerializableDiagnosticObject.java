package ee.ttu.java.studenttester.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.ttu.java.studenttester.core.enums.SourceSetType;
import ee.ttu.java.studenttester.core.helpers.ClassUtils;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Locale;

public class SerializableDiagnosticObject {

    @JsonIgnore
    private Diagnostic<? extends JavaFileObject> innerDiagnosticObject;

    public String kind;
    public int lineNo;
    public int columnNo;
    public String message;
    public String code;
    public String file;
    public String hint;
    public String affected;
    public Boolean sensitive;

    public SerializableDiagnosticObject(Diagnostic<? extends JavaFileObject> diagnosticObject) {
        this.innerDiagnosticObject = diagnosticObject;
        this.kind = diagnosticObject.getKind().toString();
        this.lineNo = (int) diagnosticObject.getLineNumber();
        this.columnNo = (int) diagnosticObject.getColumnNumber();
        this.message = diagnosticObject.getMessage(Locale.getDefault());
        this.code = diagnosticObject.getCode();
        if (diagnosticObject.getSource() != null) {
            this.file = diagnosticObject.getSource().getName();
        }
        this.hint = resolveHint();
        this.sensitive = true;
    }

    public SerializableDiagnosticObject(Diagnostic<? extends JavaFileObject> diagnosticObject,
                                        TesterContext context) {
        this(diagnosticObject);
        if (this.file != null) {
            try {
                int byteCount = (int) (innerDiagnosticObject.getEndPosition() - innerDiagnosticObject.getStartPosition());
                if (byteCount >= 0) {
                    var file = new RandomAccessFile(this.file, "r");
                    var start = innerDiagnosticObject.getStartPosition();
                    while (file.read() != '\n' && start > 0) {
                        file.seek(--start);
                    }
                    this.affected = file.readLine();
                    file.close();
                }
            } catch (Exception e) {
                this.affected = String.format("<Error when reading file: %s>", e.getMessage());
            }
            this.sensitive = this.file.startsWith(context.testRoot.getAbsolutePath());
        }
    }

    public SerializableDiagnosticObject() {

    }

    @Override
    public String toString() {
        if (this.file == null) {
            return String.format("%s: %s\n", kind, message);
        }
        return String.format("%s in %s on line %d, col %s: %s\n%s%s", kind, file, lineNo, columnNo, message,
                affected == null || sensitive ? "" : String.format("->\t%s\n", affected),
                hint == null || "".equals(hint) ? "" : String.format("Hint: %s\n", hint)
            );
    }

    private String resolveHint() {
        String str = "";
        switch (this.code) {
            case "compiler.err.cant.resolve.location.args":
                str += "Does the method exist?\n";
                break;

            case "compiler.err.illegal.char":
                str += "There seems to be an encoding error.\n";
                if (lineNo == 1 && columnNo == 1) {
                    str += "The file likely contains a Byte Order Mark (BOM). Please remove it.\n";
                }
                break;

            case "compiler.err.cant.resolve.location":
                str += "Have you declared all necessary variables/types?\n";
                break;

            case "compiler.err.prob.found.req":
                str += "Casting one type to another might help.\n";
                break;

            case "compiler.err.unreachable.stmt":
                str += "Remove either the statement causing the code to be unreachable or the code itself.\n";
                break;
            case "compiler.err.class.public.should.be.in.file":
                str += "Is the file name same as the class defined in it?\n";
                break;

            case "compiler.err.unreported.exception.need.to.catch.or.throw":
                str += "Handle the exception inside the function or "
                        + "include \"throws <exception type>\" in the function's declaration.\n";
                break;

            case "compiler.err.not.stmt":
                str += "This might be a typo.\n";
                break;

            case "compiler.err.expected":
                str += "Did you miss a name or character?\n";
                break;

            case "compiler.err.invalid.meth.decl.ret.type.req":
                str += "You must specify what the function returns.\n";
                break;

            case "compiler.err.missing.ret.stmt":
                str += "The return statement may be missing or not returning a value.\n";
                break;

            case "compiler.err.premature.eof":
                str += "Part of the file might be missing.\n";
                break;

            case "compiler.err.void.not.allowed.here":
                str += "Is the function 'void' but actually should return something?\n";
                break;

            default:
                break;
        }
        return str;
    }

}
