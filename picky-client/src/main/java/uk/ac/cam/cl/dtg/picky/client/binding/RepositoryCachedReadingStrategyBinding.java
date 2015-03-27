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

import java.io.File;

import javafx.beans.property.StringProperty;

import com.google.common.base.Strings;

import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.StrategySelector;

public class RepositoryCachedReadingStrategyBinding extends AsyncBinding<CachedReadingStrategy> {

	private StringProperty serverURL;
	private StringProperty cacheDir;
	private StringProperty tmpDir;

	private String server;
	private String cacheString;
	private String tmpString;

	public RepositoryCachedReadingStrategyBinding(StringProperty serverURL, StringProperty cacheDir, StringProperty tmpDir) {
		super(serverURL, cacheDir, tmpDir);

		this.serverURL = serverURL;
		this.cacheDir = cacheDir;
		this.tmpDir = tmpDir;
	}

	@Override
	protected CachedReadingStrategy computeValue() {
		server = serverURL.get();
		cacheString = cacheDir.get();
		tmpString = tmpDir.get();

		return super.computeValue();
	}

	protected CachedReadingStrategy doCompute() {
		System.out.println("RepositoryCachedReadingStrategyBinding.doCompute()");

		if (Strings.isNullOrEmpty(server) || Strings.isNullOrEmpty(cacheString) || Strings.isNullOrEmpty(tmpString)) return null;

		File tmp = new File(tmpString);
		File cache = new File(cacheString);

		if (!tmp.isDirectory() || !cache.isDirectory()) return null;

		ReadingRepositoryStrategy readingStrategy = StrategySelector.getReadingStrategy(server);
		System.out.println("readingStrategy: " + readingStrategy);
		FileRepositoryStrategy writingStrategy = new FileRepositoryStrategy(cache, tmp);

		return new CachedReadingStrategy(readingStrategy, writingStrategy);
	}

}
