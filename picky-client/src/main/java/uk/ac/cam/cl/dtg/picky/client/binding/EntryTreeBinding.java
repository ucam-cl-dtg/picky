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

import javafx.scene.control.CheckBoxTreeItem;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class EntryTreeBinding extends AsyncBinding<CheckBoxTreeItem<String>> {

	private FileSelectionBinding fileSelectionBinding;

	private List<FileEntry> fileList;

	public EntryTreeBinding(FileSelectionBinding fileSelectionBinding) {
		super(fileSelectionBinding);

		this.fileSelectionBinding = fileSelectionBinding;
	}

	@Override
	protected CheckBoxTreeItem<String> computeValue() {
		fileList = fileSelectionBinding.get();

		return super.computeValue();
	}

	@Override
	protected CheckBoxTreeItem<String> doCompute() throws Exception {
		ListMultimap<String, String> entrySelectionOptions = ArrayListMultimap.create();

		if (fileList != null) {

			fileList.stream()
					.flatMap(fileEntry -> fileEntry.getBlocks().stream())
					.flatMap(block -> block.getAttributes().entrySet().stream())
					.filter(e -> e.getValue() instanceof String)
					.forEach(e -> entrySelectionOptions.put(e.getKey(), (String) e.getValue()));
		}

		return toTree(entrySelectionOptions);
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
