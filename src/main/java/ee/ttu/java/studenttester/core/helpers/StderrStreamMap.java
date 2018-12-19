package ee.ttu.java.studenttester.core.helpers;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class StderrStreamMap extends ConcurrentHashMap<Thread, ByteArrayOutputStream> {
}
