package ee.ttu.java.studenttester.core.model;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.Files;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.exceptions.StudentTesterException;
import ee.ttu.java.studenttester.core.interfaces.ReportElement;
import ee.ttu.java.studenttester.core.runners.BaseRunner;
import ee.ttu.java.studenttester.core.validators.DirectoryParameterValidator;
import ee.ttu.java.studenttester.core.validators.FileParameterValidator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Object that accumulates all information from runners.
 * A new temporary directory is created for every new instance, so close() should be called to clean it up.
 */
public class TesterContext implements AutoCloseable {

    @Parameter(
            names = {"--code", "-c"},
            required = true,
            description = "Path where the testable code is located",
            validateValueWith = DirectoryParameterValidator.class,
            order = 1
    )
    public File contentRoot;

    @Parameter(
            names = {"--tests", "-t"},
            required = true,
            description = "Path where the matching unit tests are located",
            validateValueWith = DirectoryParameterValidator.class,
            order = 1
    )
    public File testRoot;

    @Parameter(
            names = {"--temp", "-tmp"},
            description = "Path used for temporary files, using system-provided location by default",
            order = 20
    )
    @JsonIgnore
    public File tempRoot = Files.createTempDir();

    @Parameter(
            names = {"--deleteTmp", "-del"},
            description = "Delete temporary folder before exit",
            order = 30
    )
    @JsonIgnore
    public boolean cleanup = false;

    @Parameter(
            names = {"--out", "-o"},
            description = "Output file name, if not defined, then stdout will be used",
            order = 10
    )
    @JsonIgnore
    public File outputFile;

    // initialized manually in ContextBuilder
    @JsonIgnore
    public Map<Enum<? extends Identifier>, BaseRunner> runners = new TreeMap<>();

    @JsonSerialize
    public Map<Enum<? extends Identifier>, ReportElement> results = new TreeMap<>();

    @Override
    public void close() {
        try {
            if (tempRoot != null && tempRoot.exists()) {
                FileUtils.deleteDirectory(tempRoot);
            }
        } catch (IOException e) {
            throw new StudentTesterException("Failed to clean up temporary folder", e);
        }
    }
}
