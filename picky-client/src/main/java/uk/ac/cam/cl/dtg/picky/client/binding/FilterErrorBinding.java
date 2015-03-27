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

import java.time.Duration;
import java.util.Map;

import javafx.beans.property.StringProperty;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.reactfx.EventStreams;

import com.google.common.base.Strings;

public class FilterErrorBinding extends AsyncBinding<String> {

	private StringProperty filterText;
	private FileContextSampleBinding fileContextSampleBinding;
	private String filter;
	private Map<String, Object> fileContextSample;

	public FilterErrorBinding(StringProperty filterText, FileContextSampleBinding fileContextSampleBinding) {
		super(fileContextSampleBinding);

		this.filterText = filterText;
		this.fileContextSampleBinding = fileContextSampleBinding;

		EventStreams.changesOf(filterText)
				.successionEnds(Duration.ofMillis(500))
				.subscribe(s -> invalidate());
	}

	@Override
	protected String computeValue() {
		filter = filterText.get();
		fileContextSample = fileContextSampleBinding.get();

		return super.computeValue();
	}

	@Override
	protected String doCompute() throws Exception {
		if (Strings.isNullOrEmpty(filter)) return "";
		if (fileContextSample == null) return null;

		// Get Engine and place native object into the context
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		fileContextSample.entrySet().forEach(es -> engine.put(es.getKey(), es.getValue()));

		// Standard Javascript dot notation prints 'now' (as it should!)
		try {
			Object result = engine.eval(filter);
			if (result instanceof Boolean) {
				return "";
			} else {
				return "Filter does not return a boolean value";
			}

		} catch (ScriptException e) {
			return e.getMessage();
		}
	}
}
