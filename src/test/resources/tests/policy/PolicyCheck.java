import com.sun.javafx.geom.AreaOp;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class PolicyCheck {

	public void whoami() {
		try {
			Process p = Runtime.getRuntime().exec("whoami");
			BufferedReader i = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = i.readLine()) != null) {
				System.err.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void procWithPath() {
		try {
			Process p = Runtime.getRuntime().exec("C:/Windows/Explorer.exe");
			BufferedReader i = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = i.readLine()) != null) {
				System.err.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exit() {
		System.exit(1);
	}

	public void innocent() {
		Math.pow(2, 2);
		System.currentTimeMillis();
	}

	public String writeAndReadInnocent(String text) {
		try {
			File f = new File("newfile.txt");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(text.getBytes());
			fos.close();
			FileInputStream fis = new FileInputStream(f);
			byte[] contents = fis.readAllBytes();
			return new String(contents, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String sniffFile() {
		return sniffPayload();

	}

	public void sniffFileAsNastyThread() {
		Thread t = new Thread() {
            @Override
            public void run() {
                sniffPayloadNastyInner();
            }
        };
        t.setName("nastyThread");
        t.start();
	}

    public int i = 0;
	private String sniffPayloadNastyInner() {
		try {
			while (true) {
				try {
					Thread.sleep(100);
					return sniffPayload();
				} catch (SecurityException e) {
					System.out.println(String.format("File read #%s denied: %s", ++i, e.toString()));
					return sniffPayload(); // continue, simulate continuing attempt to access files
				} catch (ThreadDeath td) {
					System.out.println(String.format("Thread death #%s survived", ++i));
					return sniffPayload(); // continue, simulate continuing attempt to access files
				} catch (Throwable t) {
					System.out.println(String.format("Misc exception #%s caught: %s", ++i, t.toString()));
					return sniffPayload(); // continue, simulate continuing attempt to access files
				}
			}
		} finally {
			System.out.println("In finally block, reviving thread recursively");
			return sniffPayloadNastyInner();
		}

    }

	private String sniffPayload() {
		try {
			FileInputStream fis = new FileInputStream(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
					+ "PolicyCheckTest.java"));
			byte[] contents = fis.readAllBytes();
			String str = new String(contents, "UTF-8");
			System.err.println(str);
			return str;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void hijackSecurityManager() {
		System.setSecurityManager(null);
	}

	public void openSocket() throws Exception {
		URL url = new URL("https://example.com");
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		byte[] data = is.readAllBytes();
	}

	public void reflectSafe() throws Exception {
		Field i = OtherClass.class.getDeclaredField("i");
		i.setAccessible(true);
		i.set(null, 1);
		if ((int) i.get(null) != 1) {
			throw new IllegalStateException("Expected 1, got " + i.get(null));
		}
	}

	public void reflectDangerous() throws Exception {
		Field secretField = PolicyCheckTest.class.getDeclaredField("secretField");
		secretField.setAccessible(true);
		secretField.set(null, 1);
	}


}

class OtherClass {
	private static int i = 0;
}