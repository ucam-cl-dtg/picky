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
import java.time.Duration;
import java.util.stream.Collectors;

import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.reactfx.EventStreams;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class EntrySelectionLabel extends StringBinding {

	private ObservableList<TreeItem<String>> entrySelectionProperty;
	private FileSelectionBinding fileSelectionBinding;

	private NumberFormat numberFormat = NumberFormat.getInstance();

	public EntrySelectionLabel(FileSelectionBinding fileSelectionBinding, ObservableList<TreeItem<String>> entrySelectionProperty) {
		this.fileSelectionBinding = fileSelectionBinding;
		this.entrySelectionProperty = entrySelectionProperty;

		bind(fileSelectionBinding);

		EventStreams.changesOf(entrySelectionProperty)
				.successionEnds(Duration.ofMillis(200))
				.subscribe(s -> invalidate());
	}

	@Override
	protected String computeValue() {
		ListMultimap<String, String> entrySelectionMap = ArrayListMultimap.create();

		String label = "Entry Selection";
		if (fileSelectionBinding.get() != null) {
			entrySelectionProperty
					.stream().filter(i -> i.getParent() != null)
					.forEach(i -> entrySelectionMap.put(i.getParent().getValue(), i.getValue()));

			Long fileSelection = fileSelectionBinding
					.get()
					.stream()
					.filter(e -> e.getBlocks().stream().filter(b -> b.matches(entrySelectionMap)).collect(Collectors.counting()) > 0)
					.collect(Collectors.counting());

			label += " (Matching files: " + numberFormat.format(fileSelection) + " of "
					+ numberFormat.format(fileSelectionBinding.get().size()) + ")";
		}

		return label;
	}
}
