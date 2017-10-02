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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.StrategySelector;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;
import uk.ac.cam.cl.dtg.picky.engine.Engine;
import uk.ac.cam.cl.dtg.picky.planner.Plan;
import uk.ac.cam.cl.dtg.picky.planner.Planner;
import uk.ac.cam.cl.dtg.picky.util.InRepositoryFunction;

public class CLIApp {

	private final Logger log = LoggerFactory.getLogger(CLIApp.class);

	private CLIOptions options;

	public static void main(String[] args) throws IOException {
		CLIOptions options = new CLIOptions(args);

		CLIApp createIndexApp = new CLIApp(options);
		createIndexApp.run();
	}

	public CLIApp(CLIOptions options) {
		this.options = options;
	}

	public void run() throws IOException {
		// LoggerConfigurer.configureConsoleAppender(options.getLogLevel());
		options.validate();

		File cache = new File(options.getCache());
		File tmp = new File(options.getTmp());
		File target = new File(options.getTarget());

		ReadingRepositoryStrategy readingStrategy = StrategySelector.getReadingStrategy(options.getUrl());
		FileRepositoryStrategy cacheStrategy = new FileRepositoryStrategy(cache, tmp);

		CachedReadingStrategy cachedReadingStrategy = new CachedReadingStrategy(readingStrategy, cacheStrategy);

		ReadingRepository repository = new ReadingRepository(cachedReadingStrategy);

		log.info("Resolving dataset reference");

		String datasetId = repository.resolveReference(options.getReference());

		if (datasetId == null || !repository.contains(datasetId)) {
			log.error("<" + options.getReference() + "> does not reference a dataset");
			return;
		}

		log.info("Retrieving dataset index");
		Dataset dataset = repository.retrieve(datasetId, Dataset.class);

		log.info("Dataset index retrieved");
		log.info("Id: " + dataset.getId());
		log.info("Description: " + dataset.getDescription());
		log.info("Content: " + dataset.getContentDescription());

		File selection;
		if (Strings.isNullOrEmpty(options.getSelectionFile()) || !new File(options.getSelectionFile()).exists()) {
			selection = createDefaultSelectionFile(dataset);
		} else {
			selection = new File(options.getSelectionFile());
		}

		ListMultimap<String, String> entrySelectionMap = ArrayListMultimap.create();
		List<String> entrySelection = Files.readAllLines(selection.toPath());
		entrySelection.stream().filter(s -> s.endsWith("=true")).forEach(s -> {
			entrySelectionMap.put("" + s.subSequence(0, s.indexOf(".")), "" + s.subSequence(s.indexOf(".") + 1, s.indexOf("=")));
		});

		System.out.println("Entry selection: " + entrySelectionMap);

		// FIXME:
		List<FileEntry> fileSelection = new ArrayList<>();
		fileSelection.addAll(dataset.getFiles());

		InRepositoryFunction inCache = new InRepositoryFunction(new ReadingRepository(cacheStrategy));

		Planner planner = new Planner(fileSelection, entrySelectionMap, inCache, target);
		Plan plan = planner.plan();

		printDetails(plan);

		System.out.println("Apply changes? (yes/no)");
		Scanner scanner = new Scanner(System.in);

		if (scanner.next().equalsIgnoreCase("y") || scanner.next().equalsIgnoreCase("yes")) {
			execute(cachedReadingStrategy, plan);
		}

		scanner.close();

		System.out.println("Exiting...");

	}

	private void execute(CachedReadingStrategy repository, Plan plan) {
		Engine engine = new Engine(repository, plan);
		// engine.addListener(new ProgressListener() {
		//
		// @Override
		// public void onActionStart(ProgressEvent event) {
		// System.out.println(event);
		// }
		//
		// @Override
		// public void onActionFinished(ProgressEvent event) {
		// System.out.println(event);
		// }
		// });

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new DownloadProgressTask(repository.getRepository()), 1000, 1000);

		engine.execute();

		// Analytics.sendAnalytics(plan, "start_applying_changes");
	}

	private void printDetails(Plan plan) {

		System.out.println("===================================");
		System.out.println("Dirs to delete:\t\t" + plan.getDeleteDirActions().size());
		System.out.println("Files to delete:\t" + plan.getDeleteFileActions().size());
		System.out.println("Files to update:\t" + plan.getUpdateFileActions().size());
		System.out.println("Dirs to create:\t\t" + plan.getMakeDirActions().size());
		System.out.println("Files to install:\t" + plan.getInstallFileActions().size());
		System.out.println("Chunks to download:\t" + plan.getChunksToDownload().size());
		System.out.println("Subset size: \t " + FileSizeFormatter.formateBytes(plan.getSubsetSize(), 1));
		System.out.println("===================================");
	}

	private File createDefaultSelectionFile(Dataset dataset) throws IOException {
		log.info("No selection file specified - creating default from dataset");

		File file = new File(dataset.getId() + "_selection.properties");
		log.info("Default selection file is " + file);

		Properties properties = new Properties();
		ListMultimap<String, String> entrySelectionOptions = ArrayListMultimap.create();
		dataset.getFiles().stream()
				.flatMap(fileEntry -> fileEntry.getBlocks().stream())
				.flatMap(block -> block.getAttributes().entrySet().stream())
				.filter(e -> e.getValue() instanceof String)
				.filter(e -> !entrySelectionOptions.get(e.getKey()).contains(e.getValue()))
				.forEach(e -> entrySelectionOptions.put(e.getKey(), (String) e.getValue()));

		entrySelectionOptions.keySet().forEach(k -> {
			List<String> values = entrySelectionOptions.get(k);

			for (String v : values) {
				properties.put(k + "." + v, "false");
			}
		});

		properties.store(new FileOutputStream(file), "Automatially created - delete to recreate");

		return file;
	}

	private final class DownloadProgressTask extends TimerTask {
		long bytesReadLastTime = 0;
		private ReadingRepositoryStrategy repository;

		public DownloadProgressTask(ReadingRepositoryStrategy repository) {
			this.repository = repository;
		}

		@Override
		public void run() {
			long currentRead = repository.bytesRead();
			long diff = bytesReadLastTime <= currentRead ? currentRead - bytesReadLastTime : currentRead;
			bytesReadLastTime = currentRead;

			System.out
					.println("Download: " + FileSizeFormatter.formateBytes(currentRead, 1) + " (" + FileSizeFormatter.formateBytes(diff, 1) + "/s)");
		}
	}

}
