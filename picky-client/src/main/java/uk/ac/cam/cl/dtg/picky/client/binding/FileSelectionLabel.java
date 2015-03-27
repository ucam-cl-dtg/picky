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

import java.text.NumberFormat;
import java.util.List;

import javafx.beans.binding.StringBinding;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;

public class FileSelectionLabel extends StringBinding {

	private FileSelectionBinding fileSelectionBinding;
	private DatasetBinding datasetBinding;
	private NumberFormat numberFormat = NumberFormat.getInstance();

	public FileSelectionLabel(DatasetBinding datasetBinding, FileSelectionBinding fileSelectionBinding) {
		this.datasetBinding = datasetBinding;
		this.fileSelectionBinding = fileSelectionBinding;

		bind(fileSelectionBinding);
		bind(datasetBinding);
	}

	@Override
	protected String computeValue() {
		List<FileEntry> fileSelection = fileSelectionBinding.get();
		Dataset dataset = datasetBinding.get();

		String label = "File Selection";
		if (fileSelection != null && dataset != null) {
			label += " (" + numberFormat.format(fileSelection.size()) + " of " + numberFormat.format(dataset.getFiles().size()) + ")";
		}

		return label;
	}

}
