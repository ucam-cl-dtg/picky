package uk.ac.cam.cl.dtg.picky.client.binding;

/*
 * #%L
 * Picky
 * %%
 * Copyright (C) 2017 Daniel Hintze <dh526@cl.cam.ac.uk>
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

import java.io.File;

import javafx.beans.property.StringProperty;
import uk.ac.cam.cl.dtg.picky.util.InRepositoryFunction;

import com.google.common.base.Strings;

import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;

public class InCacheBinding extends AsyncBinding<InRepositoryFunction> {

	private StringProperty cacheDir;
	private StringProperty tmpDir;

	private String cacheString;
	private String tmpString;

	public InCacheBinding(StringProperty cacheDir, StringProperty tmpDir) {
		super(cacheDir, tmpDir);

		this.cacheDir = cacheDir;
		this.tmpDir = tmpDir;
	}

	@Override
	protected InRepositoryFunction computeValue() {
		cacheString = cacheDir.get();
		tmpString = tmpDir.get();

		return super.computeValue();
	}

	protected InRepositoryFunction doCompute() {
		if (Strings.isNullOrEmpty(cacheString) || Strings.isNullOrEmpty(tmpString)) return null;

		File tmp = new File(tmpString);
		File cache = new File(cacheString);

		if (!tmp.isDirectory() || !cache.isDirectory()) return null;

		return new InRepositoryFunction(new ReadingRepository(new FileRepositoryStrategy(cache, tmp)));
	}
}
