import jarhello.JarHello;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JarHelloInvokerTest {
	@Test
	public void testInvoke() {
		JarHelloInvoker invoker = new JarHelloInvoker();
		assertEquals("hello", invoker.invoke());

		JarHello hello = new JarHello();
		assertEquals("hello", hello.hello());
	}
}