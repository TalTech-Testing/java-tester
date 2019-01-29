// from: https://github.com/junit-team/junit4/wiki/getting-started

import static org.junit.Assert.assertEquals;

import ee.ttu.java.studenttester.annotations.Gradable;
import ee.ttu.java.studenttester.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.enums.ReportMode;
import org.junit.Assert;
import org.junit.Test;

@TestContextConfiguration(mode = ReportMode.VERBOSE, welcomeMessage = "hello", identifier = 4)
public class CalculatorTestAnnotated {

    @Test
    @Gradable(weight = 3)
    public void evaluatesExpression() {
        Calculator calculator = new Calculator();
        int sum = calculator.evaluate("1+2+3");
        assertEquals(6, sum);
    }

    @Test
    @Gradable(weight = 2, description = "desc")
    public void fail() {
        Assert.fail();
    }

}