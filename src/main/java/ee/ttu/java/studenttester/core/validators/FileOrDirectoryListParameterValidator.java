package ee.ttu.java.studenttester.core.validators;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.List;

public class FileOrDirectoryListParameterValidator implements IValueValidator<List<File>> {

    @Override
    public void validate(String name, List<File> values) throws ParameterException {
        values.stream()
                .filter(value -> !value.exists())
                .findFirst()
                .ifPresent(value -> {
                    throw new ParameterException(String.format("Entry %s does not exist!", value));
                });
    }

}
