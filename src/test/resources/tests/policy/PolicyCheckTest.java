import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.MalformedURLException;

// SECRET MARKER DO NOT DELETE LINE BELOW
// SUCCESSFULHACKFLAG

public class PolicyCheckTest {

	private static int secretField = 0;

	PolicyCheck r = new PolicyCheck();

	@Test(expectedExceptions = SecurityException.class)
	public void testWhoami() {
		r.whoami();
	}

	@Test(expectedExceptions = SecurityException.class)
	public void testExit() {
		r.exit();
	}

	@Test
	public void testValidAction() {
		r.innocent();
	}

	@Test
	public void testValidWriteRead() {
		String result = r.writeAndReadInnocent("lolwat");
		Assert.assertEquals(result, "lolwat");
	}

	@Test(expectedExceptions = SecurityException.class)
	public void testProcWithPath() {
		r.procWithPath();
	}

	@Test(expectedExceptions = SecurityException.class)
	public void testSniffFile() {
		r.sniffFile();
	}

	@Test // can't probably kill this
	public void testSniffFileAsNastyThread() {
		r.sniffFileAsNastyThread();
	}

	@Test(expectedExceptions = SecurityException.class)
	public void testHijackSecurityManager() {
		r.hijackSecurityManager();
	}

	@Test
	@Ignore
	public void testOpenSocket() throws Exception {
		// default implementation should succeed
		// also fails if no Internet connection
		r.openSocket();
	}

	@Test(expectedExceptions = SecurityException.class)
	public void testOpenSocketBad() throws Exception {
		r.openSocket();
	}

	public void testReflectSafe() throws Exception {
		r.reflectSafe();
	}

	@Test(expectedExceptions = SecurityException.class)
	public void reflectDangerous() throws Exception {
		r.reflectDangerous();
		Assert.assertEquals(secretField, 0);
	}
}