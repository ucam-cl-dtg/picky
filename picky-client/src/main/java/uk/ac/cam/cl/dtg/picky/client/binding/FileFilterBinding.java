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

import javafx.beans.property.StringProperty;

import org.reactfx.EventStreams;

public class FileFilterBinding extends AsyncBinding<String> {

	private StringProperty filterText;
	private String filter;

	private FilterErrorBinding filterErrorBinding;
	private String error;

	public FileFilterBinding(StringProperty filterText, FilterErrorBinding filterErrorBinding) {
		this.filterText = filterText;
		this.filterErrorBinding = filterErrorBinding;

		EventStreams.changesOf(filterText)
				.successionEnds(Duration.ofMillis(500))
				.subscribe(s -> invalidate());

		EventStreams.changesOf(filterErrorBinding)
				.successionEnds(Duration.ofMillis(500))
				.subscribe(s -> invalidate());
	}

	@Override
	protected String computeValue() {
		filter = filterText.get();
		error = filterErrorBinding.get();

		return super.computeValue();
	}

	@Override
	protected String doCompute() throws Exception {
		if (error != "") return null;
		else return filter;
	}
}
