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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import uk.ac.cam.cl.dtg.picky.parser.Entry;

@SuppressWarnings("unused")
public class Packet {
	private static final int ETHERNET_TYPE_OFFSET = 12;
	private static final int ETHERNET_TYPE_IPV4 = 0x800;

	private static final int IP_PROT_TCP = 6;
	private static final int IP_PROT_UDP = 17;

	private static final int OFFSET_PACKET_TYPE = 0;
	private static final int OFFSET_MAC_DESC = OFFSET_PACKET_TYPE + 2;
	private static final int OFFSET_MAC_SRC = OFFSET_MAC_DESC + 6;
	private static final int OFFSET_ETHERTYPE = OFFSET_MAC_SRC + 6;
	private static final int OFFSET_ETHERNET_PAYLOAD = OFFSET_ETHERTYPE + 2;

	private static final String KEY_ETHER_TYPE = "ETHER_TYPE";
	private static final String KEY_SRC_ADR = "ADR_SRC";
	private static final String KEY_DEST_ADR = "ADR_DEST";
	private static final String KEY_IP_PROTOCOL = "IP_PROTOCOL";
	private static final String KEY_SRC_PORT = "SRC_PORT";
	private static final String KEY_DEST_PORT = "DEST_PORT";

	private byte[] buffer;
	private long etherType;
	private PackageHeader packageHeader;
	private InetAddress sourceAddress;
	private InetAddress destinationAddress;
	private int internetHeaderLength = -1;
	private int ipProtocol = -1;
	private int sourcePort = -1;
	private int destinationPort = -1;

	public Packet(PackageHeader packageHeader, byte[] buffer, boolean swapped) throws UnknownHostException {
		this.packageHeader = packageHeader;
		this.buffer = buffer;

		// FIXME: what is wrong with the etherType?

		System.out.println(packageHeader);
		this.etherType = ByteUtil.readInt(buffer, 2, OFFSET_ETHERTYPE, false);
		System.out.println(etherType);

		if (etherType == ETHERNET_TYPE_IPV4) {
			internetHeaderLength = (int) (ByteUtil.readInt(buffer, 1, OFFSET_ETHERNET_PAYLOAD, false) & 0x0F) * 4; // times 4-byte words
			sourceAddress = InetAddress
					.getByAddress(Arrays.copyOfRange(buffer, OFFSET_ETHERNET_PAYLOAD + 12, OFFSET_ETHERNET_PAYLOAD + 16));
			destinationAddress = InetAddress.getByAddress(Arrays.copyOfRange(buffer, OFFSET_ETHERNET_PAYLOAD + 16,
					OFFSET_ETHERNET_PAYLOAD + 20));
			ipProtocol = (int) ByteUtil.readInt(buffer, 1, OFFSET_ETHERNET_PAYLOAD + 9, false);
		}

		if (ipProtocol == IP_PROT_TCP || ipProtocol == IP_PROT_UDP) {
			int headerOffset = OFFSET_ETHERNET_PAYLOAD + internetHeaderLength;

			sourcePort = (int) (ByteUtil.readInt(buffer, 2, headerOffset, false));
			destinationPort = (int) (ByteUtil.readInt(buffer, 2, headerOffset + 2, false));
		}
	}

	public Entry getEntry() {
		byte[] header = packageHeader.getHeader();
		byte[] data = Arrays.copyOf(header, header.length + buffer.length);
		System.arraycopy(buffer, 0, data, header.length, buffer.length);

		Entry entry = new Entry(data);

		entry.getAttributes().put(KEY_ETHER_TYPE, String.format("%04x", (0xFFFF & etherType)).toUpperCase());

		// if (sourceAddress != null) entry.getAttributes().put(KEY_SRC_ADR, sourceAddress.getHostAddress());
		// if (destinationAddress != null) entry.getAttributes().put(KEY_DEST_ADR, destinationAddress.getHostAddress());
		if (ipProtocol >= 0) entry.getAttributes().put(KEY_IP_PROTOCOL, IPProtocols.getKeyword(ipProtocol));
		if (destinationPort >= 0) entry.getAttributes().put(KEY_DEST_PORT, "" + destinationPort);

		return entry;
	}
}
