package ee.ttu.java.studenttester.core.models.reports;

import ee.ttu.java.studenttester.core.annotations.Identifier;

public class PlainTextReport extends AbstractReport {

    @Override
    public Identifier getIdentifier() {
        return Identifier.REPORT;
    }

    @Override
    public int getCode() {
        return Integer.MAX_VALUE;
    }

    public String output;
}
