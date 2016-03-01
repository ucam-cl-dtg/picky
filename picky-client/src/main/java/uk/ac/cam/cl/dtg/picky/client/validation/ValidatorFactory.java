package uk.ac.cam.cl.dtg.picky.client.validation;

/*
 * #%L
 * Picky
 * %%
 * Copyright (C) 2015 - 2016 Daniel Hintze <dh526@cl.cam.ac.uk>
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
