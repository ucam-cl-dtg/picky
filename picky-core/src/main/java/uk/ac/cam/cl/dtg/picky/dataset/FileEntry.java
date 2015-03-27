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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.util.IO;
import uk.ac.cam.cl.dtg.picky.util.Loop;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;

public class FileEntry {

	public static final String PATH_KEY = "path";

	private static final byte CURRENT_BINARY_VERSION = 1;
	private static final byte EOF = Byte.MIN_VALUE;

	private long lastModified;
	private List<Block> blocks;
	private Attributes attributes;
	private byte[] fileHeader;

	public FileEntry(String path, long lastModified, List<Block> blocks, Attributes attributes, byte[] fileHeader) {
		this.lastModified = lastModified;
		this.blocks = blocks;
		this.attributes = attributes;
		this.fileHeader = fileHeader;
		this.attributes.put(PATH_KEY, path);
	}

	public static Optional<FileEntry> fromStream(DataInputStream inputStream) {
		try {
			byte binaryVersion = inputStream.readByte();

			if (binaryVersion == EOF) {
				return Optional.empty();
			} else {
				return Optional.of(new FileEntry(binaryVersion, inputStream));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private FileEntry(byte binaryVersion, DataInputStream inputStream) {
		IO.wrap(() -> {
			Preconditions.checkArgument(binaryVersion == CURRENT_BINARY_VERSION, "Unsupported binary version <" + binaryVersion + ">");

			lastModified = inputStream.readLong();
			fileHeader = new byte[inputStream.readInt()];
			inputStream.readFully(fileHeader);
			blocks = new ArrayList<>();
			Loop.doFor(() -> inputStream.readInt(), () -> blocks.add(new Block(inputStream)));

			attributes = Attributes.fromStream(inputStream);
		});
	}

	public void writeToStream(DataOutputStream dataOutputStream) {
		IO.wrap(() -> {
			dataOutputStream.writeByte(CURRENT_BINARY_VERSION);

			dataOutputStream.writeLong(lastModified);
			dataOutputStream.writeInt(fileHeader.length);
			dataOutputStream.write(fileHeader);
			dataOutputStream.writeInt(blocks.size());
			blocks.stream().forEach(block -> IO.wrap(() -> block.writeToStream(dataOutputStream)));
			attributes.writeToStream(dataOutputStream);
		});
	}

	public static void writeEOF(DataOutputStream dataOutputStream) {
		IO.wrap(() -> dataOutputStream.writeByte(EOF));
	}

	public long getLastModified() {
		return lastModified;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public byte[] getFileHeader() {
		return fileHeader;
	}

	public boolean matches(ListMultimap<String, String> selection) {
		return Attributes.haveCommonValues(attributes, selection);
	}
}
