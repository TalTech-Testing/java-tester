package ee.ttu.java.studenttester.core.validators;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;

public class DirectoryParameterValidator implements IValueValidator<File> {

    @Override
    public void validate(String name, File value) throws ParameterException {
        if (value.exists() && value.isDirectory()) {
            return;
        }
        throw new ParameterException(String.format("Directory %s does not exist!", value));
    }

}
