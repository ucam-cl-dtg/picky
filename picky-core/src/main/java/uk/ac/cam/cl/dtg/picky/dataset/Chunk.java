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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.ac.cam.cl.dtg.picky.util.IO;

import com.google.common.base.Preconditions;

import de.ecclesia.kipeto.common.util.Assert;

public class Chunk {

	private static final byte CURRENT_BINARY_VERSION = 1;

	private String blobId;
	private long lengthUncompressed;
	private long lengthCompressed;
	private long entries;

	public Chunk(String blobId, long lengthUncompressed, long lengthCompressed, long entries) {
		this.blobId = blobId;
		this.lengthUncompressed = lengthUncompressed;
		this.lengthCompressed = lengthCompressed;
		this.entries = entries;
	}

	public Chunk(DataInputStream inputStream) {
		try {
			byte binaryVersion = inputStream.readByte();

			Preconditions.checkArgument(binaryVersion == CURRENT_BINARY_VERSION, "Unsupported binary version <" + binaryVersion + ">");

			blobId = inputStream.readUTF();
			lengthUncompressed = inputStream.readLong();
			lengthCompressed = inputStream.readLong();
			entries = inputStream.readLong();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeToStream(DataOutputStream dataOutputStream) {
		Assert.isNotNull(blobId, "Item '" + (blobId) + "' has no Id (has it yet been written to a repository?)");

		IO.wrap(() -> {
			dataOutputStream.writeByte(CURRENT_BINARY_VERSION);
			dataOutputStream.writeUTF(blobId);
			dataOutputStream.writeLong(lengthUncompressed);
			dataOutputStream.writeLong(lengthCompressed);
			dataOutputStream.writeLong(entries);
		});
	}

	public String getBlobId() {
		return blobId;
	}

	public long getLengthUncompressed() {
		return lengthUncompressed;
	}

	public long getLengthCompressed() {
		return lengthCompressed;
	}

	@Override
	public int hashCode() {
		return blobId.hashCode();
	}

	public long getEntries() {
		return entries;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Chunk other = (Chunk) obj;
		if (blobId == null) {
			if (other.blobId != null) return false;
		} else if (!blobId.equals(other.blobId)) return false;
		return true;
	}

}
