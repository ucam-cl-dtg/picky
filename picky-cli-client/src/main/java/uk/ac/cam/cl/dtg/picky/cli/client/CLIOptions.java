package uk.ac.cam.cl.dtg.picky.cli.client;

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

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import com.google.common.base.Strings;

public class CLIOptions {

	@Option(name = "-l", aliases = { "--logLevel" }, usage = "Log Level")
	private String logLevel;

	@Option(name = "-t", aliases = { "--target" }, usage = "Target directory to replicate dataset into (e.g. '/local/dataset')", metaVar = "DIR", required = true)
	private String target;

	@Option(name = "-c", aliases = { "--cache" }, usage = "Directory to cache downloaded parts (e.g. '/local/cache')", metaVar = "DIR", required = true)
	private String cache;
	
	@Option(name = "-m", aliases = { "--tmp" }, usage = "Tmp directory (e.g. '/tmp')", metaVar = "DIR", required = true)
	private String tmp;

	@Option(name = "-u", aliases = { "--url" }, usage = "Repositoy URL", metaVar = "URL")
	private String url;

	@Option(name = "-r", aliases = { "--reference" }, usage = "Unique reference name", metaVar = "REF", required = true)
	private String reference;

	@Option(name = "-s", aliases = { "--selection" }, usage = "Selection file (e.g. '/local/selection.json')", metaVar = "FILE")
	private String selectionFile;
	
	public CLIOptions(String[] args) {
		parse(args);
	}

	protected void parse(String[] args) {
		CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults().withUsageWidth(130));

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			System.err.println();

			System.exit(1);
		}
	}

	public Level getLogLevel() {
		return Level.toLevel(logLevel, Level.INFO);
	}
	
	public void validate() {
		validateDirectory(cache);
		validateDirectory(tmp);
		validateDirectory(target);
	}
	
	private void validateDirectory(String file) {
		if(Strings.isNullOrEmpty(file) || !new File(file).isDirectory()) {
			throw new RuntimeException(file + " is not a directory");
		}
	}

	public String getReference() {
		return reference;
	}

	public String getTarget() {
		return target;
	}

	public String getCache() {
		return cache;
	}

	public String getTmp() {
		return tmp;
	}

	public String getUrl() {
		return url;
	}

	public String getSelectionFile() {
		return selectionFile;
	}
}
