package ee.ttu.java.studenttester.core.runners;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.annotations.Runnable;
import ee.ttu.java.studenttester.core.enums.RunnerResultType;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.helpers.StreamRedirector;
import ee.ttu.java.studenttester.core.models.TesterContext;
import ee.ttu.java.studenttester.core.models.reports.PlainTextReport;

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

    @Parameter(
            names = {"--plainTextOutputInJson", "-jsontxt"},
            description = "Include the plain text report in JSON (overrides --plainTextOutput).",
            order = 10
    )
    private Boolean plainTextOutputInJson = false;

    private PlainTextReport plainTextReport = new PlainTextReport();

    public ReportRunner(TesterContext context) {
        super(context);
    }

    @Override
    public void run() throws StudentTesterException {
        StreamRedirector.reset();
        plainTextReport.result = RunnerResultType.SUCCESS;
        try {
            if (plainTextOutput || plainTextOutputInJson) {
                plainTextReport.output = context.results.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining());
            }
            if (plainTextOutput && !plainTextOutputInJson) {
                if (context.outputFile != null) {
                    var writer = new PrintWriter(context.outputFile);
                    writer.write(plainTextReport.output);
                    writer.close();
                } else {
                    System.out.println(plainTextReport.output);
                }
            } else {
                if (plainTextOutputInJson) {
                    context.results.putResult(plainTextReport);
                }
                if (context.outputFile != null) {
                    mapper.writeValue(context.outputFile, context);
                } else {
                    System.out.println(mapper
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
        System.out.flush(); // in case any output is pending
    }

}
