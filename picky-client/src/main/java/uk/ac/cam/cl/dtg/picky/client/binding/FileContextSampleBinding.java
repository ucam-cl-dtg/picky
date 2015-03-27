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

import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.ObjectBinding;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;

public class FileContextSampleBinding extends AsyncBinding<Map<String, Object>> {

	private Dataset dataset;

	private ObjectBinding<Dataset> datasetBinding;

	public FileContextSampleBinding(ObjectBinding<Dataset> datasetBinding) {
		super(datasetBinding);

		this.datasetBinding = datasetBinding;
	}

	@Override
	protected Map<String, Object> computeValue() {
		dataset = datasetBinding.get();

		return super.computeValue();
	}

	@Override
	protected Map<String, Object> doCompute() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		if (dataset != null) {
			dataset.getFiles().stream().flatMap(fileEntry -> fileEntry.getAttributes().entrySet().stream())
					.forEach(e -> map.put(e.getKey(), e.getValue()));
		}

		return map;
	}
}
