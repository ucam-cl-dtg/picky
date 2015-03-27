package uk.ac.cam.cl.dtg.picky.engine;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Queue;

import uk.ac.cam.cl.dtg.picky.dataset.Block;
import uk.ac.cam.cl.dtg.picky.dataset.Chunk;
import uk.ac.cam.cl.dtg.picky.engine.Engine.IRepositoryCallback;
import uk.ac.cam.cl.dtg.picky.util.IO;
import de.ecclesia.kipeto.common.util.Streams;
import de.ecclesia.kipeto.repository.Blob;

public class BlockInputSource implements Comparable<BlockInputSource> {
	private Queue<Chunk> chunks;

	private IRepositoryCallback repository;

	private byte[] currentData;
	private long currentSequenceId = Long.MAX_VALUE;

	private Chunk currentChunk;
	private ObjectInputStream currentInputStream;
	private long readsFromCurrentChunk;

	public BlockInputSource(IRepositoryCallback repository, Block block) {
		this.repository = repository;
		this.chunks = new LinkedList<Chunk>(block.getChunks());

		IO.wrap(() -> readNext());
	}

	public byte[] read() {
		byte[] currentData = this.currentData;

		IO.wrap(() -> readNext());

		return currentData;
	}

	public boolean hasNext() {
		return currentData != null;
	}

	private void readNext() throws IOException {
		currentData = null;
		currentSequenceId = Long.MAX_VALUE;

		// Read next chunk into buffer
		if (currentInputStream == null && !chunks.isEmpty()) {
			currentChunk = chunks.poll();
			readsFromCurrentChunk = 0;
			Blob blob = repository.getOrDownload(currentChunk.getBlobId());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			Streams.copyStream(blob.contentStream(), byteArrayOutputStream, true);

			currentInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
		}

		if (currentInputStream != null) {
			currentSequenceId = currentInputStream.readLong();
			currentData = new byte[currentInputStream.readInt()];
			currentInputStream.readFully(currentData);

			if (++readsFromCurrentChunk == currentChunk.getEntries()) {
				currentInputStream = null;
			}
		}
	}

	@Override
	public int compareTo(BlockInputSource other) {
		return Long.compare(currentSequenceId, other.currentSequenceId);
	}

}
