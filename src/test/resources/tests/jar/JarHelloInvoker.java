import jarhello.JarHello;

public class JarHelloInvoker {
	public String invoke() {
		return new JarHello().hello();
	}
}
