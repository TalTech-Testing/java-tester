// from: https://github.com/junit-team/junit4/wiki/getting-started

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CalculatorTestBad {
    @Test
    public void evaluatesExpression() {
        Calculator calculator = new Calculator();
        int sum = calculator.evaluate"1+2+3");
        assertEquals(6, sum);
    }
}