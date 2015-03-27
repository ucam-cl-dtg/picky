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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class FileContextLabelBinding extends AsyncBinding<List<Label>> {

	private static final String ATTRIBUTE = "ATTRIBUTE";

	private Dataset dataset;

	private ObjectBinding<Dataset> datasetBinding;

	public FileContextLabelBinding(ObjectBinding<Dataset> datasetBinding) {
		super(datasetBinding);

		this.datasetBinding = datasetBinding;
	}

	@Override
	protected List<Label> computeValue() {
		dataset = datasetBinding.get();

		return super.computeValue();
	}

	@Override
	protected List<Label> doCompute() throws Exception {
		ListMultimap<String, String> fileAttributeTypes = ArrayListMultimap.create();
		ListMultimap<String, Object> fileAttributeValues = ArrayListMultimap.create();

		if (dataset != null) {
			fileAttributeTypes.put("path", String.class.getSimpleName());

			dataset.getFiles()
					.stream()
					.flatMap(fileEntry -> fileEntry.getAttributes().entrySet().stream())
					.forEach(e -> {
						fileAttributeTypes.put(e.getKey(), e.getValue().getClass().getSimpleName());
						fileAttributeValues.put(e.getKey(), e.getValue());
					});
		}

		List<Label> labels = fileAttributeTypes
				.keySet()
				.stream()
				.sorted()
				.map(attribute -> {
					Label label = new Label(attribute + " : " + new HashSet<String>(fileAttributeTypes.get(attribute)));
					label.getProperties().put(ATTRIBUTE, attribute);
					return label;
				})
				.collect(Collectors.toList());

		// As of now, toolstips need to be created in FX application thread...
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				labels.stream().forEach(label -> {
					Tooltip toolTip = new Tooltip(Stream.concat(fileAttributeValues.get(label
							.getProperties().get(ATTRIBUTE).toString())
							.stream()
							.distinct()
							.limit(10)
							.sorted()
							.map(Object::toString), Stream.of("..."))
							.collect(Collectors.toList()).toString());
					toolTip.setWrapText(true);
					label.setTooltip(toolTip);
				});
			}
		});

		return labels;
	}
}
