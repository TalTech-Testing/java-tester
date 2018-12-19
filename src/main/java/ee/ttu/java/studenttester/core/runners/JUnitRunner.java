package ee.ttu.java.studenttester.core.runners;

import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.model.TesterContext;

@Runnable(identifier = Identifier.JUNIT, order = 10, enabled = false)
public class JUnitRunner extends BaseRunner {

    public JUnitRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws StudentTesterException {
        // TODO
    }

    @Override
    public void commit() {
        context.results.put(Identifier.JUNIT, null);
    }

}
