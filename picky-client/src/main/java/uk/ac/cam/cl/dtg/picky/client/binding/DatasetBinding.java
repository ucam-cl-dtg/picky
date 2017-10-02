package uk.ac.cam.cl.dtg.picky.client.binding;

import java.util.List;

import com.google.common.base.Strings;

import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;

public class DatasetBinding extends AsyncBinding<Dataset> implements IStatusProvider {

	private String datasetReference;
	private CachedReadingStrategy readingStrategy;
	private StringProperty datasetDir;
	private ObjectBinding<CachedReadingStrategy> repositoryBinding;
	private StringProperty status = new SimpleStringProperty();

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
	public StringProperty statusProperty() {
		return status;
	}

	@Override
	protected Dataset doCompute() throws Exception {
		setStatus("");
		try {
			if (readingStrategy == null || Strings.isNullOrEmpty(datasetReference)) return null;

			ReadingRepository repository = new ReadingRepository(readingStrategy);

			setStatus("Resolving dataset reference");
			String datasetId = repository.resolveReference(datasetReference);

			if (datasetId == null || !repository.contains(datasetId)) {
				throw new RuntimeException("<" + datasetReference + "> does not reference a dataset");
			}

			setStatus("Retrieving dataset index");
			Dataset dataset = repository.retrieve(datasetId, Dataset.class);

			// printDetails(dataset);

			setStatus("");

			return dataset;
		} catch (Exception e) {
			setStatus(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void setStatus(String statusText) {
		Platform.runLater(() -> status.set(statusText));
	}

	@SuppressWarnings("unused")
	private void printDetails(Dataset dataset) {
		List<FileEntry> files = dataset.getFiles();

		long numberOfChunks = files.stream()
				.flatMap(f -> f.getBlocks().stream())
				.flatMap(b -> b.getChunks().stream())
				.count();

		double avgBlocks = files.stream()
				.mapToLong(f -> f.getBlocks().size())
				.average().getAsDouble();

		long numberOfEntries = files.stream()
				.flatMap(f -> f.getBlocks().stream())
				.flatMap(b -> b.getChunks().stream())
				.mapToLong(c -> c.getEntries())
				.sum();

		System.out.println("Dataset:" + dataset.getContentDescription());
		System.out.println("Number of Chunks:" + numberOfChunks);
		System.out.println("Number of Entries:" + numberOfEntries);
		System.out.println("Avg number of blocks: " + avgBlocks);

	}

}
