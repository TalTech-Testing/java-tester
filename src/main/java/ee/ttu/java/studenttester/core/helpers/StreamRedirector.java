package ee.ttu.java.studenttester.core.helpers;

import java.io.*;

public class StreamRedirector {

    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;
    private static final InputStream STDIN = System.in;

    private static MapOutputStream<StdoutStreamMap> stdoutMapOutputStream
            = new MapOutputStream<>(StdoutStreamMap::new);
    private static MapOutputStream<StderrStreamMap> stderrMapOutputStream
            = new MapOutputStream<>(StderrStreamMap::new);

    public static void beginRedirect() {
        System.setOut(new PrintStream(stdoutMapOutputStream));
        System.setErr(new PrintStream(stderrMapOutputStream));
    }

    public static void enableNullStdin() {
        System.setIn(InputStream.nullInputStream());
    }

    /**
     * Reset all streams to their default state.
     */
    public static void reset() {
        stdoutMapOutputStream = new MapOutputStream<>(StdoutStreamMap::new);
        stderrMapOutputStream = new MapOutputStream<>(StderrStreamMap::new);
        System.setOut(STDOUT);
        System.setErr(STDERR);
        System.setIn(STDIN);
    }

    public static StdoutStreamMap getStdoutStreams() {
        return stdoutMapOutputStream.getStreamMap();
    }

    public static StderrStreamMap getStderrStreams() {
        return stderrMapOutputStream.getStreamMap();
    }

    private StreamRedirector() {

    }

}
