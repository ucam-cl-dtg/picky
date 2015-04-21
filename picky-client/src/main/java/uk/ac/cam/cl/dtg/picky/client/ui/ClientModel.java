package uk.ac.cam.cl.dtg.picky.client.ui;

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

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import uk.ac.cam.cl.dtg.picky.client.binding.AsyncBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.CacheRepositoryBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.DatasetBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.EntrySelectionLabel;
import uk.ac.cam.cl.dtg.picky.client.binding.EntryTreeBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FileContextLabelBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FileContextSampleBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FileFilterBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FileSelectionBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FileSelectionLabel;
import uk.ac.cam.cl.dtg.picky.client.binding.FileTreeBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.FilterErrorBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.PlanBinding;
import uk.ac.cam.cl.dtg.picky.client.binding.RepositoryCachedReadingStrategyBinding;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.planner.Plan;
import de.ecclesia.kipeto.repository.CachedReadingStrategy;

public class ClientModel {

	private PlanBinding planBinding;
	private DatasetBinding datasetBinding;
	private FileTreeBinding fileTreeBinding;
	private FileFilterBinding filterBinding;
	private EntryTreeBinding entryTreeBinding;
	private FileSelectionLabel fileSelectionLabel;
	private FilterErrorBinding filterErrorBinding;
	private EntrySelectionLabel entrySelectionLabel;
	private FileSelectionBinding fileSelectionBinding;
	private CacheRepositoryBinding cacheRepositoryBinding;
	private FileContextSampleBinding fileContextSampleBinding;
	private RepositoryCachedReadingStrategyBinding repositoryBinding;
	private FileContextLabelBinding fileContextLabelBinding;
	private ObservableList<Label> fileContextLabels = FXCollections.<Label> observableArrayList();
	private ObservableList<TreeItem<String>> entrySelection;
	private StringProperty serverURL;

	public ClientModel(
			StringProperty serverURL,
			StringProperty datasetURL,
			StringProperty targetDir,
			StringProperty cacheDir,
			StringProperty tmpDir,
			StringProperty fileFilter,
			ObservableList<TreeItem<String>> entrySelection) {

		this.repositoryBinding = new RepositoryCachedReadingStrategyBinding(serverURL, cacheDir, tmpDir);
		this.datasetBinding = new DatasetBinding(datasetURL, repositoryBinding);
		this.fileTreeBinding = new FileTreeBinding(datasetBinding);
		this.cacheRepositoryBinding = new CacheRepositoryBinding(cacheDir, tmpDir);
		this.fileContextSampleBinding = new FileContextSampleBinding(datasetBinding);
		this.filterErrorBinding = new FilterErrorBinding(fileFilter, fileContextSampleBinding);
		this.filterBinding = new FileFilterBinding(fileFilter, filterErrorBinding);
		this.fileContextLabelBinding = new FileContextLabelBinding(datasetBinding);
		this.fileSelectionBinding = new FileSelectionBinding(datasetBinding, filterBinding);
		this.entryTreeBinding = new EntryTreeBinding(fileSelectionBinding);
		this.fileSelectionLabel = new FileSelectionLabel(datasetBinding, fileSelectionBinding);
		this.entrySelectionLabel = new EntrySelectionLabel(fileSelectionBinding, entrySelection);
		this.entrySelection = entrySelection;
		this.serverURL = serverURL;
		this.fileContextLabelBinding.addListener(new ChangeListener<List<Label>>() {

			@Override
			public void changed(ObservableValue<? extends List<Label>> observable, List<Label> oldValue, List<Label> newValue) {
				if (newValue == null) {
					fileContextLabels.clear();
				} else {
					fileContextLabels.addAll(newValue);
				}
			}

		});

		this.planBinding = new PlanBinding(cacheRepositoryBinding, targetDir, fileSelectionBinding, entrySelection);
	}

	public AsyncBinding<Plan> getPlanBinding() {
		return planBinding;
	}

	public AsyncBinding<CachedReadingStrategy> getRepositoryBinding() {
		return repositoryBinding;
	}

	public AsyncBinding<CheckBoxTreeItem<String>> getFileTreeBinding() {
		return fileTreeBinding;
	}

	public AsyncBinding<CheckBoxTreeItem<String>> getEntryTreeBinding() {
		return entryTreeBinding;
	}

	public AsyncBinding<Dataset> getDatasetBinding() {
		return datasetBinding;
	}

	public ObservableList<TreeItem<String>> getEntrySelection() {
		return entrySelection;
	}

	public ObservableList<Label> getFileContextLabels() {
		return fileContextLabels;
	}

	public FilterErrorBinding getFilterErrorBinding() {
		return filterErrorBinding;
	}

	public FileFilterBinding getFilterBinding() {
		return filterBinding;
	}

	public FileSelectionLabel getFileSelectionLabel() {
		return fileSelectionLabel;
	}

	public EntrySelectionLabel getEntrySelectionLabel() {
		return entrySelectionLabel;
	}

	public StringProperty getServerURL() {
		return serverURL;
	}

	public FileSelectionBinding getFileSelectionBinding() {
		return fileSelectionBinding;
	}

}
