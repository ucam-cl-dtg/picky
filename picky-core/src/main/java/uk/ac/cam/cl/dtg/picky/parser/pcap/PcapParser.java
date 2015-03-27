package uk.ac.cam.cl.dtg.picky.parser.pcap;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.parser.Entry;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

import com.google.common.base.Preconditions;

public class PcapParser implements IEntryParser {

	private static final Logger LOG = LoggerFactory.getLogger(PcapParser.class);

	private static final int GLOBAL_HEADER_SIZE = 24;
	private static final int PACKAGE_HEADER_SIZE = 16;
	private InputStream stream;

	private GlobalHeader globalHeader;

	private AtomicInteger atomicInteger = new AtomicInteger();

	private byte[] packageHeaderBuffer = new byte[PACKAGE_HEADER_SIZE];

	@Override
	public void open(InputStream stream) {
		this.stream = stream;
		this.globalHeader = readGlobalHeader();
	}

	@Override
	public Optional<Entry> readEntry() {
		try {
			int read = stream.read(packageHeaderBuffer);
			if (read == -1) return Optional.empty();
			if (atomicInteger.incrementAndGet() > 10) return Optional.empty();

			Preconditions.checkArgument(read == PACKAGE_HEADER_SIZE, "Could not read packet header");

			PackageHeader packageHeader = new PackageHeader(globalHeader, packageHeaderBuffer);

			byte[] buffer = new byte[packageHeader.getLength()];
			read = stream.read(buffer);
			Preconditions.checkArgument(read == packageHeader.getLength(), "Could not read packet");
			Packet packet = new Packet(packageHeader, buffer, globalHeader.isByteOrderSwapped());

			return Optional.of(packet.getEntry());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Attributes getFileAttributes() {
		Attributes attributes = new Attributes();

		return attributes;
	}

	@Override
	public byte[] getFileHeader() {
		return globalHeader.getHeader();
	}

	@Override
	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private GlobalHeader readGlobalHeader() {
		try {
			byte[] buffer = new byte[GLOBAL_HEADER_SIZE];
			int read = stream.read(buffer);

			Preconditions.checkArgument(read == buffer.length, "Could not read global header");

			return new GlobalHeader(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
