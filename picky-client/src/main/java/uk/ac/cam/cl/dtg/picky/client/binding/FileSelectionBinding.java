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
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.binding.ObjectBinding;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.dataset.BlockFactory;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;

public class FileSelectionBinding extends AsyncBinding<List<FileEntry>> {

	private static final Logger LOG = LoggerFactory.getLogger(BlockFactory.class);

	private Dataset dataset;

	private ObjectBinding<Dataset> datasetBinding;
	private FileFilterBinding filterBinding;

	private String filter;
	private ScriptEngine engine;
	private Set<String> engineAttributes = new HashSet<>();

	public FileSelectionBinding(ObjectBinding<Dataset> datasetBinding, FileFilterBinding filterBinding) {
		super(datasetBinding, filterBinding);

		this.datasetBinding = datasetBinding;
		this.filterBinding = filterBinding;
		this.engine = new ScriptEngineManager().getEngineByName("JavaScript");
	}

	@Override
	protected List<FileEntry> computeValue() {
		dataset = datasetBinding.get();
		filter = filterBinding.get();

		return super.computeValue();
	}

	@Override
	protected List<FileEntry> doCompute() throws Exception {
		if (dataset == null || filter == null) return null;

		System.out.println("FileSelectionBinding.doCompute()");

		return dataset.getFiles()
				.stream()
				.filter(this::include)
				.collect(Collectors.toList());
	}

	private boolean include(FileEntry entry) {
		if (filter.isEmpty()) return true;

		try {
			// Remove all attributes that will be overwritten anyway
			engineAttributes.removeAll(entry.getAttributes().keySet());

			// Remove attributes from engine that whould not be overwritten
			engineAttributes.forEach(a -> engine.getContext().removeAttribute(a, ScriptContext.ENGINE_SCOPE));

			// remember attributes in context for next iteration to avoid having stale attributes
			engineAttributes.addAll(entry.getAttributes().keySet());

			// set file attributes as attributes in js engine context
			entry.getAttributes().entrySet().forEach(es -> engine.put(es.getKey(), es.getValue()));

			Object result = engine.eval(filter);
			if (result instanceof Boolean) {
				return (boolean) result;
			} else {
				LOG.error("Filter does not return a boolean value: " + filter + ", " + entry);
			}

		} catch (ScriptException e) {
			LOG.error(e.getMessage(), e);
		}

		return false;

	}

}
