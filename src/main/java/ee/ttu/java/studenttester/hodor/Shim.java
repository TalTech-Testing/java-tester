package ee.ttu.java.studenttester.hodor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ttu.java.studenttester.core.StudentTester;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Shim {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = Logger.getLogger(StudentTester.class.getName());

    public static void checkRunViaShim() {
        TesterInput input = null;
        try {
            var reader = new BufferedReader(new InputStreamReader(System.in));
            if (reader.ready()) {
                input = mapper.readValue(reader, TesterInput.class);
                System.err.println(String.format("Read input via stdin, overriding default:\n"
                        + "\ttestRoot:\t%s\n"
                        + "\tcontentRoot:\t%s\n"
                        + "\textra:\t\t%s\n",
                        input.testRoot, input.contentRoot, input.extra));
                if (input.contentRoot == null || input.testRoot == null) {
                    throw new StudentTesterException("Missing path detected, cannot continue!");
                }
            }
        } catch (IOException e) {
            LOG.severe("Failed to read input from stdin:");
            e.printStackTrace();
        }
        if (input != null) {
            var args = splitExtra(input.extra);
            args.addAll(List.of("-t", input.testRoot, "-c", input.contentRoot, "-r", "COMPILER,TESTNG,REPORT"));
            System.err.println("Complete command line: " + args);
            StudentTester.main(args.toArray(String[]::new));
            System.exit(0);
        }
    }

    private static List<String> splitExtra(String extra) {
        if (extra == null) {
            return new ArrayList<>();
        }
        var splitArgs = new ArrayList<String>();
        var regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        var matcher = regex.matcher(extra);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                splitArgs.add(matcher.group(1)); // "
            } else if (matcher.group(2) != null) {
                splitArgs.add(matcher.group(2)); // '
            } else {
                // Add unquoted word
                splitArgs.add(matcher.group());
            }
        }
        return splitArgs;
    }
}
