package ee.ttu.java.studenttester.core.annotations;

public enum Identifier {

    CHECKSTYLE("checkstyle"),
    JUNIT("junit"),
    COMPILER("compile"),
    TESTNG("testng"),
    REPORT("report");

    private String value;

    Identifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
