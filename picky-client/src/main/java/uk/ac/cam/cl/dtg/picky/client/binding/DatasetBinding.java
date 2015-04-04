package uk.ac.cam.cl.dtg.picky.client.binding;

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

import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.StringProperty;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;

import com.google.common.base.Strings;

import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;

public class DatasetBinding extends AsyncBinding<Dataset> {

	private String datasetReference;
	private CachedReadingStrategy readingStrategy;
	private StringProperty datasetDir;
	private ObjectBinding<CachedReadingStrategy> repositoryBinding;

	public DatasetBinding(StringProperty datasetDir, ObjectBinding<CachedReadingStrategy> repositoryBinding) {
		super(datasetDir, repositoryBinding);

		this.datasetDir = datasetDir;
		this.repositoryBinding = repositoryBinding;
	}

	@Override
	protected Dataset computeValue() {
		datasetReference = datasetDir.get();
		readingStrategy = repositoryBinding.get();

		return super.computeValue();
	}

	@Override
	protected Dataset doCompute() throws Exception {
		if (readingStrategy == null || Strings.isNullOrEmpty(datasetReference)) return null;

		ReadingRepository repository = new ReadingRepository(readingStrategy);
		String datasetId = repository.resolveReference(datasetReference);

		if (datasetId == null || !repository.contains(datasetId)) {
			throw new RuntimeException("<" + datasetReference + "> does not reference a dataset");
		}

		Dataset dataset = repository.retrieve(datasetId, Dataset.class);

		printDetails(dataset);

		return dataset;
	}

	private void printDetails(Dataset dataset) {
		List<FileEntry> files = dataset.getFiles();

		long numberOfChunks = files.stream()
				.flatMap(f -> f.getBlocks().stream())
				.flatMap(b -> b.getChunks().stream())
				.count();

		double avgBlocks = files.stream()
				.mapToLong(f -> f.getBlocks().size())
				.average().getAsDouble();

		System.out.println("Dataset:" + dataset.getContentDescription());
		System.out.println("Number of Chunks:" + numberOfChunks);
		System.out.println("Avg number of blocks: " + avgBlocks);

	}

}
