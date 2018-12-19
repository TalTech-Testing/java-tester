package ee.ttu.java.studenttester.core.exceptions;

public class StudentTesterException extends RuntimeException {

    public StudentTesterException(String message, Exception e) {
        super(message, e);
    }

    public StudentTesterException(String message) {
        super(message);
    }

    public StudentTesterException(Exception exception) {
        super(exception);
    }

}
