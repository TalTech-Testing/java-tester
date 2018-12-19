// from: https://github.com/junit-team/junit4/wiki/getting-started

import static org.junit.Assert.assertEquals;

import ee.ttu.java.studenttester.core.annotations.Gradeable;
import ee.ttu.java.studenttester.core.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.core.enums.ReportMode;
import org.junit.Assert;
import org.junit.Test;

@TestContextConfiguration(mode = ReportMode.VERBOSE, welcomeMessage = "hello", identifier = 4)
public class CalculatorTestAnnotated {

    @Test
    @Gradeable(weight = 3)
    public void evaluatesExpression() {
        Calculator calculator = new Calculator();
        int sum = calculator.evaluate("1+2+3");
        assertEquals(6, sum);
    }

    @Test
    @Gradeable(weight = 2, description = "desc")
    public void fail() {
        Assert.fail();
    }

}