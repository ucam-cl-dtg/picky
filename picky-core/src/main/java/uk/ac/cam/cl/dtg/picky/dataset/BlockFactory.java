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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.parser.Entry;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.WritingRepository;

public class BlockFactory {

	private static final Logger LOG = LoggerFactory.getLogger(BlockFactory.class);

	private IEntryParser entryParser;
	private WritingRepository repository;

	private Map<Attributes, ChunkBuffer> chunkBuffers = new HashMap<>();
	private ListMultimap<Attributes, Chunk> chunkReferences = ArrayListMultimap.create();

	private long sequenceId;

	private long maxChunkSize;

	public static FileEntry create(IEntryParser entryParser, WritingRepository repository, String path, long lastModified, long maxChunkSize) {
		return new BlockFactory(entryParser, repository, maxChunkSize).createFileEntry(path, lastModified);
	}

	private BlockFactory(IEntryParser entryParser, WritingRepository repository, long maxChunkSize) {
		this.entryParser = entryParser;
		this.repository = repository;
		this.maxChunkSize = maxChunkSize;
	}

	public FileEntry createFileEntry(String path, long lastModified) {
		Optional<Entry> optionalEntry;

		while ((optionalEntry = entryParser.readEntry()).isPresent()) {
			process(optionalEntry.get());
		}

		// Write remaining chunks to disc
		for (Attributes attributes : new HashSet<>(chunkBuffers.keySet())) {
			writeChunk(chunkBuffers.get(attributes), attributes);
		}

		List<Block> blocks = createBlocks();

		FileEntry fileEntry = new FileEntry(path, lastModified, blocks, entryParser.getFileAttributes(), entryParser.getFileHeader());

		return fileEntry;
	}

	private List<Block> createBlocks() {
		return chunkReferences
				.keySet()
				.stream()
				.map(a -> new Block(a, chunkReferences.get(a)))
				.collect(Collectors.toList());
	}

	private void process(Entry entry) {
		Attributes attributes = entry.getAttributes();
		ChunkBuffer chunkBuffer = getChunkBufferFor(attributes);
		chunkBuffer.write(sequenceId++, entry.getData());

		if (chunkBuffer.isFull()) {
			writeChunk(chunkBuffer, attributes);
		}
	}

	private void writeChunk(ChunkBuffer chunkBuffer, Attributes attributes) {
		try {
			File chunkFile = chunkBuffer.getFile();
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(chunkFile));

			Blob chunk = new PrecompressedGZIPBlob(inputStream, chunkBuffer.getUncompressedSize());
			repository.store(chunk);
			chunkFile.delete();

			LOG.trace("written chunk {} size {}", chunk.id(), chunkBuffer.getCompressedSize());

			chunkReferences.get(attributes).add(
					new Chunk(chunk.id(),
							chunkBuffer.getUncompressedSize(),
							chunkBuffer.getCompressedSize(),
							chunkBuffer.getEntries()));
			chunkBuffers.remove(attributes);
			// chunkReferences.keySet().stream().map(a -> new Block(a,
			// chunkReferences.get(a))).collect(Collectors.toList());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ChunkBuffer getChunkBufferFor(Attributes attributes) {
		if (!chunkBuffers.containsKey(attributes)) {
			LOG.trace("Adding chunk buffer " + chunkBuffers.size() + " for " + attributes);
			chunkBuffers.put(attributes, new ChunkBuffer(maxChunkSize));
		}

		return chunkBuffers.get(attributes);
	}

}
