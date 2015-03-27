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

// http://wiki.wireshark.org/Development/LibpcapFileFormat#File_Format
public class GlobalHeader {

	public static final long PCAP_MAGIC_NUMBER_NATURAL_ORDERING = 0xA1B2C3D4;
	public static final long PCAP_MAGIC_NUMBER_SWAPPED_ORDERING = 0xD4C3B2A1;

	public static final long PCAP_MAGIC_NUMBER_NANO_NATURAL_ORDERING = 0xA1B23C4D;
	public static final long PCAP_MAGIC_NUMBER_NANO_SWAPPED_ORDERING = 0x4D3CB2A1;

	/* magic number */
	private long magic_number;

	/* major version number */
	private long version_major;

	/* minor version number */
	private long version_minor;

	/* GMT to local correction */
	private long thiszone;

	/* accuracy of timestamps */
	private long sigfigs;

	/* max length of captured packets, in octets */
	private long snaplen;

	/* data link type */
	private long network;

	private LinkType linkType;

	private boolean byteOrderSwapped;
	private boolean nanoSeconds;
	private byte[] header;

	public GlobalHeader(byte[] header) {
		this.header = header;
		magic_number = ByteUtil.readInt(header, 4, 0, false);

		byteOrderSwapped = (magic_number == PCAP_MAGIC_NUMBER_SWAPPED_ORDERING || magic_number == PCAP_MAGIC_NUMBER_NANO_SWAPPED_ORDERING);
		nanoSeconds = (magic_number == PCAP_MAGIC_NUMBER_NANO_NATURAL_ORDERING || magic_number == PCAP_MAGIC_NUMBER_NANO_SWAPPED_ORDERING);

		version_major = ByteUtil.readInt(header, 2, 4, byteOrderSwapped);
		version_minor = ByteUtil.readInt(header, 2, 6, byteOrderSwapped);
		thiszone = ByteUtil.readInt(header, 4, 8, byteOrderSwapped);
		sigfigs = ByteUtil.readInt(header, 4, 12, byteOrderSwapped);
		snaplen = ByteUtil.readInt(header, 4, 16, byteOrderSwapped);
		network = ByteUtil.readInt(header, 4, 20, byteOrderSwapped);

		linkType = LinkType.findById((int) network);
	}

	public boolean isByteOrderSwapped() {
		return byteOrderSwapped;
	}

	public byte[] getHeader() {
		return header;
	}

	@Override
	public String toString() {
		return "GlobalHeader [magic_number=" + magic_number + ", version_major=" + version_major + ", version_minor=" + version_minor
				+ ", thiszone=" + thiszone + ", sigfigs=" + sigfigs + ", snaplen=" + snaplen + ", network=" + network + ", linkType="
				+ linkType + ", byteOrderSwapped=" + byteOrderSwapped + ", nanoSeconds=" + nanoSeconds + "]";
	}

}
