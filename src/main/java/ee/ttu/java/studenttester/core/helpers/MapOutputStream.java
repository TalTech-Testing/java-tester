package ee.ttu.java.studenttester.core.helpers;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MapOutputStream<T extends ConcurrentHashMap<Thread, ByteArrayOutputStream>> extends OutputStream {

    private static final int INITIAL_CAPACITY = 8192;
    private T streamMap;

    public MapOutputStream(Supplier<T> supplier) {
        streamMap = supplier.get();
    }

    @Override
    public void write(int b) {
        var thread = Thread.currentThread();
        if (!streamMap.containsKey(thread)) {
            streamMap.put(thread, new ByteArrayOutputStream(INITIAL_CAPACITY));
        }
        streamMap.get(thread).write(b);
    }

    public T getStreamMap() {
        return streamMap;
    }

    private MapOutputStream() {

    }

}