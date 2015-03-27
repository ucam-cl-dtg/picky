package uk.ac.cam.cl.dtg.picky.dataset;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

import com.google.common.base.Preconditions;

import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.WritingRepository;

public class DatasetConfig {

	private final File tmpDir;
	private final File logDir;
	private final File sourceDir;
	private final File repositoryDir;

	private final String url;
	private final byte[] icon;
	private final Level logLevel;
	private final String reference;
	private final String description;
	private final long chunkSizeLimit;
	private final WritingRepository repository;
	private final Class<? extends IEntryParser> entryParser;

	private DatasetConfig(DatasetConfigBuilder builder) {
		this.tmpDir = builder.tmpDir;
		this.logDir = builder.logDir;
		this.sourceDir = builder.sourceDir;
		this.repositoryDir = builder.repositoryDir;
		this.url = builder.url;
		this.reference = builder.reference;
		this.description = builder.description;
		this.chunkSizeLimit = builder.chunkSizeLimit;
		this.entryParser = builder.entryParser;
		this.logLevel = builder.logLevel;
		this.icon = builder.icon;
		this.repository = builder.createRepository();
	}

	public File getTmpDir() {
		return tmpDir;
	}

	public File getLogDir() {
		return logDir;
	}

	public File getSourceDir() {
		return sourceDir;
	}

	public File getRepositoryDir() {
		return repositoryDir;
	}

	public String getUrl() {
		return url;
	}

	public String getReference() {
		return reference;
	}

	public String getDescription() {
		return description;
	}

	public byte[] getIcon() {
		return icon;
	}

	public long getChunkSizeLimit() {
		return chunkSizeLimit;
	}

	public Class<? extends IEntryParser> getEntryParser() {
		return entryParser;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public WritingRepository getRepository() {
		return repository;
	}

	public static class DatasetConfigBuilder {
		private final Logger log = LoggerFactory.getLogger(DatasetConfigBuilder.class);

		private File tmpDir;
		private File logDir;
		private File sourceDir;
		private File repositoryDir;

		private String url;
		private String reference;
		private String description;
		private byte[] icon = new byte[0];
		private long chunkSizeLimit = 5 * 1024 * 1024;
		private Class<? extends IEntryParser> entryParser;
		private Level logLevel = Level.INFO;

		public DatasetConfigBuilder tmpDir(File tmpDir) {

			this.tmpDir = tmpDir;
			return this;
		}

		public DatasetConfigBuilder logDir(File logDir) {
			this.logDir = logDir;
			return this;
		}

		public DatasetConfigBuilder sourceDir(File sourceDir) {
			this.sourceDir = sourceDir;
			return this;
		}

		public DatasetConfigBuilder repositoryDir(File repositoryDir) {
			this.repositoryDir = repositoryDir;
			return this;
		}

		public DatasetConfigBuilder url(String url) {
			this.url = url;
			return this;
		}

		public DatasetConfigBuilder reference(String reference) {
			this.reference = reference;
			return this;
		}

		public DatasetConfigBuilder description(String description) {
			this.description = description;
			return this;
		}

		public DatasetConfigBuilder icon(byte[] icon) {
			this.icon = icon;
			return this;
		}

		public DatasetConfigBuilder chunkSizeLimit(long chunkSizeLimit) {
			if (chunkSizeLimit > 0) this.chunkSizeLimit = chunkSizeLimit;
			return this;
		}

		public DatasetConfigBuilder entryParser(Class<? extends IEntryParser> entryParser) {
			this.entryParser = entryParser;
			return this;
		}

		public DatasetConfigBuilder logLevel(Level logLevel) {
			this.logLevel = logLevel;
			return this;
		}

		public WritingRepository createRepository() {
			return new WritingRepository(new FileRepositoryStrategy(repositoryDir, tmpDir));
		}

		public DatasetConfig build() {
			ensureDirExists(tmpDir);
			ensureDirExists(logDir);
			ensureDirExists(repositoryDir);

			Preconditions.checkArgument(sourceDir.isDirectory(), sourceDir.getAbsolutePath() + " is not a directory");

			return new DatasetConfig(this);
		}

		private void ensureDirExists(File dir) {
			Preconditions.checkArgument(!dir.isFile(), dir.getAbsolutePath() + " is not a directory");

			if (!dir.exists()) {
				log.info(dir.getPath() + " does not exist; creating");
				Preconditions.checkState(dir.mkdirs(), "could not create " + dir.getPath());
			}
		}
	}

}
