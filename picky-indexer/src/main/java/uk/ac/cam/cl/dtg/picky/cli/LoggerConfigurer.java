package uk.ac.cam.cl.dtg.picky.cli;

/*
 * #%L
 * Picky
 * %%
 * Copyright (C) 2015 Daniel Hintze <dh526@cl.cam.ac.uk>
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerConfigurer {

	private static final PatternLayout LAYOUT = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %p %c %x - %m%n");
	private static final PatternLayout SIMPLE_LAYOUT = new PatternLayout("[%-5p] %m%n");

	private static final String[] PACKAGES = { "de.ecclesia", "uk.ac.cam.cl.dtg" };

	public static FileAppender configureFileAppender(File logDir, String fileName) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss-SSS");
		Logger logger = Logger.getRootLogger();
		FileAppender appender = null;

		try {
			String logFile = new File(logDir.getAbsolutePath(), fileName + simpleDateFormat.format(new Date()) + ".log").getAbsolutePath();
			appender = new FileAppender(LAYOUT, logFile, true);
			appender.setName("LOGFILE");

			logger.addAppender(appender);
			logger.setLevel(Level.INFO);

			Arrays.stream(PACKAGES).map(Logger::getLogger).forEach(l -> l.setLevel(Level.DEBUG));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Printing ERROR Statements", e);
		}

		return appender;
	}

	public static void configureConsoleAppender(Level level) {
		ConsoleAppender consoleAppender = new ConsoleAppender(LAYOUT);
		consoleAppender.setName("CONSOLE");

		if (level.isGreaterOrEqual(Level.INFO)) {
			consoleAppender.setLayout(SIMPLE_LAYOUT);
		} else {
			consoleAppender.setLayout(LAYOUT);
		}

		consoleAppender.setThreshold(level);

		Arrays.stream(PACKAGES).map(Logger::getLogger).forEach(l -> l.addAppender(consoleAppender));
	}
}
