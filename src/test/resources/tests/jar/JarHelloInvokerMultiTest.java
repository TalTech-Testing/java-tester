import jarhello.JarHello;
import jarbye.JarBye;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JarHelloInvokerMultiTest {
	@Test
	public void testInvoke() {
		JarHelloInvokerMulti invoker = new JarHelloInvokerMulti();
		assertEquals("hello", invoker.invoke());
		assertEquals("bye", invoker.invokeBye());

		JarHello hello = new JarHello();
		JarBye bye = new JarBye();
		assertEquals("hello", hello.hello());
		assertEquals("bye", bye.bye());
	}
}