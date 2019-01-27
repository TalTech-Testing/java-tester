package ee.ttu.java.studenttester.core.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.models.TesterInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FileArgsReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = Logger.getLogger(FileArgsReader.class.getName());

    public static String[] getArgsFromFile(File file) {
        TesterInput input = null;
        try {
            input = mapper.readValue(file, TesterInput.class);
            LOG.info(String.format("Read input via file:\n"
                            + "\ttestRoot:\t\t%s\n"
                            + "\tcontentRoot:\t%s\n"
                            + "\textra:\t\t\t%s\n",
                    input.testRoot, input.contentRoot, input.extra));
            if (input.contentRoot == null || input.testRoot == null) {
                throw new StudentTesterException("Missing path detected, cannot continue!");
            }
        } catch (IOException e) {
            LOG.severe("Failed to read input from file:");
            e.printStackTrace();
        }
        if (input != null) {
            var args = splitExtra(input.extra);
            args.addAll(List.of("-c", input.contentRoot, "-t", input.testRoot));
            LOG.info("Args from file: " + args);
            return args.toArray(String[]::new);
        }
        return new String[0];
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
