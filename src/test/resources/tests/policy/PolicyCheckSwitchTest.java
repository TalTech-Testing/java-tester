import ee.ttu.java.studenttester.annotations.TestContextConfiguration;
import ee.ttu.java.studenttester.core.enums.TesterPolicy;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.MalformedURLException;

@TestContextConfiguration(disablePolicies = TesterPolicy.DISABLE_SOCKETS)
public class PolicyCheckSwitchTest {

	PolicyCheckSwitch r = new PolicyCheckSwitch();

	@Test
	public void testOpenSocketBad() throws Exception {
		r.openSocket();
	}

}