package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.model.TesterContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.stream.Collectors;

@Runnable(identifier = Identifier.REPORT, order = 50)
public class ReportRunner extends BaseRunner {

    private ObjectMapper mapper = new ObjectMapper();

    @Parameter(
            names = {"--plainTextOutput", "-txt"},
            description = "Instead of JSON, print a plain text report",
            order = 10
    )
    private Boolean plainTextOutput = false;

    public ReportRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws StudentTesterException {
        try {
            if (plainTextOutput) {
                String plainTextOutput = context.results.values().stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining());
                if (context.outputFile != null) {
                    var writer = new PrintWriter(context.outputFile);
                    writer.write(plainTextOutput);
                    writer.close();
                } else {
                    System.out.print(plainTextOutput);
                }
            } else {
                if (context.outputFile != null) {
                    mapper.writeValue(context.outputFile, context);
                } else {
                    System.out.print(mapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(context));
                }

            }
        } catch (IOException e) {
            LOG.severe("Unable to write output, results will not be available");
            e.printStackTrace();
        }

    }

    @Override
    public void commit() {
        // all is done
    }

}
