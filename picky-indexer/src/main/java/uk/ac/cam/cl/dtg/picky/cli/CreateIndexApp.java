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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.DatasetConfig;
import uk.ac.cam.cl.dtg.picky.dataset.DatasetFactory;
import de.ecclesia.kipeto.repository.WritingRepository;

public class CreateIndexApp {

	private final Logger log = LoggerFactory.getLogger(CreateIndexApp.class);

	private DatasetConfig config;

	public static void main(String[] args) throws IOException {
		CreateIndexOptions options = new CreateIndexOptions(args);

		DatasetConfig config = options.createConfig();

		CreateIndexApp createIndexApp = new CreateIndexApp(config);
		createIndexApp.run();
	}

	public CreateIndexApp(DatasetConfig config) {
		this.config = config;
	}

	public void run() throws IOException {
		LoggerConfigurer.configureFileAppender(config.getLogDir(), "create_index");
		LoggerConfigurer.configureConsoleAppender(config.getLogLevel());

		WritingRepository repository = config.getRepository();

		DatasetFactory datasetFactory = new DatasetFactory(config);

		Dataset dataset = datasetFactory.create();
		repository.store(dataset);

		repository.createReference(config.getReference(), dataset.id());

		log.info("added dataset {} -> {}", dataset, dataset.getId());
		log.info("done");

		repository.close();
	}
}
