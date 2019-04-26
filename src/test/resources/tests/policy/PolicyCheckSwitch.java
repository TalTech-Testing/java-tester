import com.sun.javafx.geom.AreaOp;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class PolicyCheckSwitch {

	public void openSocket() throws Exception {
		try {
			URL url = new URL("http://localhost");
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			int data = is.available();
		} catch (ConnectException ce) {
			return;
		}
	}
}