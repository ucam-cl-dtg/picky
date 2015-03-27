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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.dataset.Dataset.DatasetBuilder;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

import com.google.common.io.Files;

public class DatasetFactory {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int totalNumberOfFiles;
	private AtomicInteger currentFile = new AtomicInteger();
	private DatasetConfig config;

	public DatasetFactory(DatasetConfig config) {
		this.config = config;
	}

	public Dataset create() {
		DatasetBuilder datasetBuilder = new Dataset.DatasetBuilder(config.getDescription(), config.getUrl(), config.getIcon());

		List<File> files = StreamSupport
				.stream(Files.fileTreeTraverser().preOrderTraversal(config.getSourceDir()).spliterator(), true)
				.filter(File::isFile)
				.collect(Collectors.toList());

		totalNumberOfFiles = files.size();

		ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		try {
			forkJoinPool.submit(() -> {
				files.parallelStream()
						.map(this::processFile)
						.filter(f -> f != null)
						.forEach(fileEntry -> datasetBuilder.appendFile(fileEntry));
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		// FIXME: Maintain order of files to ensure reproducable datasets?
		// .forEachOrdered(fileEntry -> datasetBuilder.appendFile(fileEntry));

		return datasetBuilder.build();
	}

	private FileEntry processFile(File file) {
		logger.debug("processing file {} of {}: {}", currentFile.incrementAndGet(), totalNumberOfFiles, file);

		File resultCacheFile = new File(config.getTmpDir(), file.getName() + "_entry");

		// FIXME: consider repeating file names
		// if (resultCacheFile.exists() && file.lastModified() == resultCacheFile.lastModified()) {
		// try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(resultCacheFile)))) {
		// logger.debug("Recovering {} from previous results", file);
		// return FileEntry.fromStream(inputStream).get();
		// } catch (IOException e) {
		// logger.error(e.getMessage(), e);
		// }
		// }

		try (InputStream inputStream = openStream(file)) {
			IEntryParser entryParser = config.getEntryParser().newInstance();
			entryParser.open(inputStream);

			// FIXME:
			String path = file.getAbsolutePath().substring(config.getSourceDir().getAbsolutePath().length() + 1).replace(".gz", "");

			FileEntry entry = BlockFactory.create(entryParser, config.getRepository(), path, file.lastModified(),
					config.getChunkSizeLimit());

			resultCacheFile.delete();
			resultCacheFile.createNewFile();
			DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(resultCacheFile)));
			entry.writeToStream(outputStream);
			outputStream.close();
			entryParser.close();
			resultCacheFile.setLastModified(file.lastModified());

			return entry;
		} catch (Exception e) {
			logger.error("Error while processing {}: {}", file, e.getMessage(), e);
			return null;
		}
	}

	private InputStream openStream(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);

		if (file.getName().toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(fileInputStream);
		} else {
			return fileInputStream;
		}
	}
}
