package ee.ttu.java.studenttester.core.helpers;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class StdoutStreamMap extends ConcurrentHashMap<Thread, ByteArrayOutputStream> {
}
