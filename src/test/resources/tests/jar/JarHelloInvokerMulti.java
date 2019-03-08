import jarhello.JarHello;
import jarbye.JarBye;

public class JarHelloInvokerMulti {
	public String invoke() {
		return new JarHello().hello();
	}

	public String invokeBye() {
		return new JarBye().bye();
	}
}