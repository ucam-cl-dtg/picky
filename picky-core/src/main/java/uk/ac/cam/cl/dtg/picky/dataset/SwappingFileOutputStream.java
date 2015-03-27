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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class SwappingFileOutputStream extends OutputStream {

	private static final int MAX_OPEN_FILE_STREAMS = 800;

	private File file;

	private FileOutputStream fileOutputStream;

	private static final AtomicInteger openFileOutputStreams = new AtomicInteger();

	public SwappingFileOutputStream(File file) {
		this.file = file;
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);

		if (openFileOutputStreams.get() > MAX_OPEN_FILE_STREAMS) {
			closeFileOutputStream();
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (fileOutputStream == null) {
			fileOutputStream = new FileOutputStream(file, true);
			openFileOutputStreams.incrementAndGet();
		}

		fileOutputStream.write(b);
	}

	@Override
	public void close() throws IOException {
		closeFileOutputStream();

		super.close();
	}

	private void closeFileOutputStream() throws IOException {
		if (fileOutputStream != null) {
			fileOutputStream.close();
			fileOutputStream = null;
			openFileOutputStreams.decrementAndGet();
		}
	}

}
