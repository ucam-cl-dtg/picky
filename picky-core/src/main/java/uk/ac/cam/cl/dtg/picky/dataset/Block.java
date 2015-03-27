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
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.util.IO;
import uk.ac.cam.cl.dtg.picky.util.Loop;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;

public class Block {

	private static final byte CURRENT_BINARY_VERSION = 1;

	private Attributes attributes;
	private List<Chunk> chunks;

	public Block(Attributes attributes, List<Chunk> chunks) {
		this.chunks = chunks;
		this.attributes = attributes;
	}

	public Block(DataInputStream inputStream) {
		IO.wrap(() -> {
			byte binaryVersion = inputStream.readByte();

			Preconditions.checkArgument(binaryVersion == CURRENT_BINARY_VERSION, "Unsupported binary version <" + binaryVersion + ">");

			attributes = Attributes.fromStream(inputStream);

			chunks = new ArrayList<>();
			Loop.doFor(() -> inputStream.readInt(), () -> chunks.add(new Chunk(inputStream)));
		});
	}

	public void writeToStream(DataOutputStream dataOutputStream) {
		IO.wrap(() -> {
			dataOutputStream.writeByte(CURRENT_BINARY_VERSION);
			attributes.writeToStream(dataOutputStream);

			dataOutputStream.writeInt(chunks.size());
			chunks.stream().forEach(chunk -> chunk.writeToStream(dataOutputStream));
		});
	}

	public boolean matches(ListMultimap<String, String> selection) {
		return Attributes.haveCommonValues(attributes, selection);
	}

	public long getLengthUncompressed() {
		return chunks.stream().mapToLong(Chunk::getLengthUncompressed).sum();
	}

	public long getLengthCompressed() {
		return chunks.stream().mapToLong(Chunk::getLengthCompressed).sum();
	}

	public List<Chunk> getChunks() {
		return chunks;
	}

	public Attributes getAttributes() {
		return attributes;
	}

}
