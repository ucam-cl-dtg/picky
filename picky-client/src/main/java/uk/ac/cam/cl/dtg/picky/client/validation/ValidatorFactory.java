package uk.ac.cam.cl.dtg.picky.client.validation;

import java.io.File;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

public class ValidatorFactory {

	public static <T> Validator<T> createIsDirectoryValidator(final Severity severity) {
		return (c, value) -> {
			boolean condition;

			if (value instanceof String && !((String) value).isEmpty()) {
				condition = !new File((String) value).isDirectory();
			} else {
				condition = false;
			}

			String message = value + " is not a directory";

			return ValidationResult.fromMessageIf(c, message, severity, condition);
		};
	}

}
