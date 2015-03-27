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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.binding.ObjectBinding;
import javafx.scene.control.CheckBoxTreeItem;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class FileTreeBinding extends AsyncBinding<CheckBoxTreeItem<String>> {

	private Dataset dataset;

	private ObjectBinding<Dataset> datasetBinding;

	public FileTreeBinding(ObjectBinding<Dataset> datasetBinding) {
		super(datasetBinding);

		this.datasetBinding = datasetBinding;
	}

	@Override
	protected CheckBoxTreeItem<String> computeValue() {
		dataset = datasetBinding.get();

		return super.computeValue();
	}

	@Override
	protected CheckBoxTreeItem<String> doCompute() throws Exception {
		ListMultimap<String, String> fileSelectionOptions = ArrayListMultimap.create();

		if (dataset != null) {
			dataset.getFiles()
					.stream()
					.flatMap(fileEntry -> fileEntry.getAttributes().entrySet().stream())
					.filter(e -> e.getValue() instanceof String)
					.forEach(e -> fileSelectionOptions.put(e.getKey(), (String) e.getValue()));
		}

		return toTree(fileSelectionOptions);
	}

	private CheckBoxTreeItem<String> toTree(ListMultimap<String, String> selection) {
		CheckBoxTreeItem<String> root = new CheckBoxTreeItem<String>("root");

		for (String key : selection.keySet()) {
			List<String> options = new ArrayList<>(new HashSet<>(selection.get(key)));
			Collections.sort(options);

			CheckBoxTreeItem<String> keyRoot = new CheckBoxTreeItem<String>(key);
			keyRoot.setExpanded(true);

			List<CheckBoxTreeItem<String>> nodes = options.stream().map(o -> new CheckBoxTreeItem<String>(o))
					.collect(Collectors.toList());
			keyRoot.getChildren().addAll(nodes);
			root.getChildren().add(keyRoot);
		}

		return root;
	}

}
