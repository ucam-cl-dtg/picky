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

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import uk.ac.cam.cl.dtg.picky.dataset.DatasetConfig;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;
import de.ecclesia.kipeto.common.util.Files;

public class CreateIndexOptions {

	@Option(name = "-l", aliases = { "--logLevel" }, usage = "Log Level")
	private String logLevel;

	@Option(name = "-t", aliases = { "--target" }, usage = "Target directory to create index in (e.g. '/local/index')", metaVar = "DIR", required = true)
	private String target;

	@Option(name = "-s", aliases = { "--source" }, usage = "Source directory to create index from (e.g. '/local/dataset')", metaVar = "DIR", required = true)
	private String source;

	@Option(name = "-m", aliases = { "--tmp" }, usage = "Tmp directory (e.g. '/tmp')", metaVar = "DIR", required = true)
	private String tmp;

	@Option(name = "-r", aliases = { "--reference" }, usage = "Unique reference name", metaVar = "REF", required = true)
	private String reference;

	@Option(name = "-n", aliases = { "--description" }, usage = "Dataset description")
	private String description;

	@Option(name = "-u", aliases = { "--url" }, usage = "Website for further information related to the dataset", metaVar = "URL")
	private String url;

	@Option(name = "-i", aliases = { "--icon" }, usage = "Dataset or provider icon")
	private String icon;

	@Option(name = "-p", aliases = { "--parser" }, usage = "Full-quallified class name of IEntryParser implementation", required = true)
	private String entryParserClass;

	@Option(name = "-c", aliases = { "--chunkSizeLimit" }, usage = "Chunk size limit in byte (default: 5MB)", required = false)
	private long chunkSizeLimit = 5 * 1024 * 1024;

	public CreateIndexOptions(String[] args) {
		parse(args);
	}

	protected void parse(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		parser.setUsageWidth(130);

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

	@SuppressWarnings("unchecked")
	public DatasetConfig createConfig() {
		try {
			return new DatasetConfig.DatasetConfigBuilder()
					.chunkSizeLimit(chunkSizeLimit)
					.description(description)
					.entryParser((Class<? extends IEntryParser>) Class.forName(entryParserClass))
					.icon(icon != null ? Files.readBytesFromFile(new File(icon)) : new byte[0])
					.url(url)
					.reference(reference)
					.sourceDir(new File(source))
					.tmpDir(new File(tmp))
					.logDir(new File(new File(target), "log"))
					.repositoryDir(new File(new File(target), "repository"))

					.build();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public String getReference() {
		return reference;
	}
}
