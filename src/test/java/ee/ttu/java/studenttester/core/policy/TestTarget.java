package ee.ttu.java.studenttester.core.policy;

public class TestTarget {

    private int value;

    private TestTarget() {
        value = 2;
    }

    private int getValue() {
        return value;
    }

    private void setValue(int value) {
        this.value = value;
    }

}