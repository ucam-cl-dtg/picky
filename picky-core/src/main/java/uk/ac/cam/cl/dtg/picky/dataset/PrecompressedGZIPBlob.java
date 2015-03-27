package uk.ac.cam.cl.dtg.picky.dataset;

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

import java.io.InputStream;

import de.ecclesia.kipeto.compressor.Compressor;
import de.ecclesia.kipeto.repository.Blob;

// Assuming the content stream is already compressed using gzip
public class PrecompressedGZIPBlob extends Blob {

	public PrecompressedGZIPBlob(String id, InputStream contentStream, Long contentLength) {
		super(id, Compressor.GZIP, contentStream, contentLength);
	}

	public PrecompressedGZIPBlob(InputStream contentStream, Long contentLength) {
		this(null, contentStream, contentLength);
	}

	@Override
	protected Compressor createCompressor(String compression) {
		return super.createCompressor(Compressor.NONE);
	}
}
