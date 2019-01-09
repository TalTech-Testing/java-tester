package ee.ttu.java.studenttester.core.exceptions;

public class StudentTesterException extends RuntimeException {

    public StudentTesterException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public StudentTesterException(String message) {
        super(message);
    }

    public StudentTesterException(Throwable throwable) {
        super(throwable);
    }

}
