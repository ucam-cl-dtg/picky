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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import de.ecclesia.kipeto.common.util.CountingOutputStream;

public class ChunkBuffer {

	private SwappingFileOutputStream swappingFileOutputStream;
	private GZIPOutputStream compressedOutputStream;
	private ObjectOutputStream objectOutputStream;
	private CountingOutputStream countingOutputStream;
	private BufferedOutputStream bufferedOutputStream;

	private long uncompressedSize = 0;
	private long entries = 0;
	private long maxChuckSize;
	private File tmp;

	public ChunkBuffer(long chuckSize) {
		this.maxChuckSize = chuckSize;
		try {
			tmp = File.createTempFile("chunk", "data");
			tmp.deleteOnExit();

			swappingFileOutputStream = new SwappingFileOutputStream(tmp);
			bufferedOutputStream = new BufferedOutputStream(swappingFileOutputStream);
			countingOutputStream = new CountingOutputStream(bufferedOutputStream);
			compressedOutputStream = new GZIPOutputStream(countingOutputStream);
			objectOutputStream = new ObjectOutputStream(compressedOutputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(long sequenceId, byte[] data) {
		try {
			objectOutputStream.writeLong(sequenceId);
			objectOutputStream.writeInt(data.length);
			objectOutputStream.write(data);
			uncompressedSize += data.length;
			entries++;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isFull() {
		return countingOutputStream.getByteCount() > maxChuckSize;
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	public long getEntries() {
		return entries;
	}

	public File getFile() {
		try {
			objectOutputStream.close();
			compressedOutputStream.finish();
			compressedOutputStream.close();
			swappingFileOutputStream.close();

			return tmp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public long getCompressedSize() {
		return countingOutputStream.getByteCount();
	}

}
