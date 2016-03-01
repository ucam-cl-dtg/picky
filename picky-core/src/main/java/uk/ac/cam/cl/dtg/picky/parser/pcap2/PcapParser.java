package uk.ac.cam.cl.dtg.picky.parser.pcap2;

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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.ByteArrayBuffer;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.FragmentedPacket;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV4DestinationUnreachablePacket;
import org.pcap4j.packet.IcmpV4EchoPacket;
import org.pcap4j.packet.IcmpV4EchoReplyPacket;
import org.pcap4j.packet.IcmpV4RedirectPacket;
import org.pcap4j.packet.IcmpV4TimeExceededPacket;
import org.pcap4j.packet.IcmpV6CommonPacket;
import org.pcap4j.packet.IcmpV6DestinationUnreachablePacket;
import org.pcap4j.packet.IcmpV6EchoReplyPacket;
import org.pcap4j.packet.IcmpV6EchoRequestPacket;
import org.pcap4j.packet.IcmpV6PacketTooBigPacket;
import org.pcap4j.packet.IcmpV6TimeExceededPacket;
import org.pcap4j.packet.IllegalPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6ExtFragmentPacket;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.parser.Entry;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

public class PcapParser implements IEntryParser {

	public static final String INTERNET_LAYER = "Internet layer";
	public static final String TRANSPORT_LAYER = "Transport layer";

	private static final Logger LOG = LoggerFactory.getLogger(PcapParser.class);

	private File tempFile;
	private PcapHandle pcap;
	private byte[] globalHeader;

	private int counter = 0;
	private File file;

	private File tmpFolder = new File("picky_tmp");

	@Override
	public void open(File file) throws IOException {
		this.file = file;
		LOG.info("opening " + file);

		tmpFolder.mkdirs();

		try {
			if (file.getName().toLowerCase().endsWith(".gz")) {
				LOG.info("decompressing " + file);
				tempFile = new File(tmpFolder, file.getName() + ".tmp");
				tempFile.delete();
				tempFile.deleteOnExit();

				GZIPInputStream stream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
				FileUtils.copyInputStreamToFile(stream, tempFile);
				stream.close();

				pcap = Pcaps.openOffline(tempFile.getAbsolutePath());
			} else {
				pcap = Pcaps.openOffline(file.getAbsolutePath());
			}

			globalHeader = readGlobalHeader(file);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<Entry> readEntry() {
		try {
			Packet packet = pcap.getNextPacketEx();
			Entry entry = toEntry(packet);

			if (++counter % 100000 == 0) {
				LOG.info("read entry " + counter + " from " + file);
			}

			return Optional.of(entry);
		} catch (PcapNativeException | NotOpenException | TimeoutException e) {
			LOG.error(e.getMessage(), e);

			return Optional.empty();
		} catch (EOFException e) {
			return Optional.empty();
		}
	}

	private Entry toEntry(Packet packet) {
		Entry entry = new Entry(toPCAPFormat(packet));

		for (Packet subPacket : packet) {
			addAttributes(entry, subPacket);
		}

		return entry;
	}

	private byte[] toPCAPFormat(Packet packet) {
		ByteArrayBuffer buffer = new ByteArrayBuffer(50);

		ByteBuffer b = ByteBuffer.allocate(16);
		b.order(ByteOrder.LITTLE_ENDIAN);

		// guint32 ts_sec; /* timestamp seconds */
		putUnsignedInt(b, pcap.getTimestamp().getTime() / 1000);

		// guint32 ts_usec; /* timestamp microseconds */
		putUnsignedInt(b, (pcap.getTimestamp().getTime() - pcap.getTimestamp().getTime() / 1000 * 1000));

		// guint32 incl_len; /* number of octets of packet saved in file */
		putUnsignedInt(b, packet.getRawData().length);

		// guint32 orig_len; /* actual length of packet */
		putUnsignedInt(b, packet.length());
		// FIXME: putUnsignedInt(b, packet.getOriginalLength());

		buffer.append(b.array(), 0, 16);
		buffer.append(packet.getRawData(), 0, packet.getRawData().length);

		return buffer.toByteArray();
	}

	private static void putUnsignedInt(ByteBuffer bb, long value) {
		bb.putInt((int) (value & 0xffffffffL));
	}

	private void addAttributes(Entry entry, Packet packet) {
		Attributes attributes = entry.getAttributes();

		if (packet instanceof IpV4Packet) {
			attributes.put(INTERNET_LAYER, "Ip4");
		} else if (packet instanceof IpV6Packet) {
			attributes.put(INTERNET_LAYER, "Ip6");
		} else if (packet instanceof TcpPacket) {
			attributes.put(TRANSPORT_LAYER, "TCP");

			TcpPacket tcpPacket = (TcpPacket) packet;
			// attributes.put("SrcPort",
			// tcpPacket.getHeader().getSrcPort().toString());
			if (tcpPacket.getHeader().getDstPort().valueAsInt() < 1024) {
				attributes.put("DstPort", tcpPacket.getHeader().getDstPort().toString());
			}

		} else if (packet instanceof UdpPacket) {
			attributes.put(TRANSPORT_LAYER, "UDP");

			UdpPacket udpPacket = (UdpPacket) packet;
			// attributes.put("SrcPort",
			// udpPacket.getHeader().getSrcPort().toString());
			if (udpPacket.getHeader().getDstPort().valueAsInt() < 1024) {
				attributes.put("DstPort", udpPacket.getHeader().getDstPort().toString());
			}
		} else if (packet instanceof IcmpV4CommonPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (CommonPacket)");
		} else if (packet instanceof IcmpV4EchoPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (EchoRequest)");
		} else if (packet instanceof IcmpV4EchoReplyPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (EchoReply)");
		} else if (packet instanceof IcmpV4RedirectPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (Redirect)");
		} else if (packet instanceof IcmpV4DestinationUnreachablePacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (DestinationUnreachable)");
		} else if (packet instanceof IcmpV4TimeExceededPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV4 (IcmpV4TimeExceeded)");
		} else if (packet instanceof IcmpV6CommonPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (CommonPacket)");
		} else if (packet instanceof IcmpV6DestinationUnreachablePacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (DestinationUnreachable)");
		} else if (packet instanceof IcmpV6TimeExceededPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (TimeExceeded)");
		} else if (packet instanceof IcmpV6EchoRequestPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (EchoRequest)");
		} else if (packet instanceof IcmpV6EchoReplyPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (EchoReply)");
		} else if (packet instanceof IcmpV6PacketTooBigPacket) {
			attributes.put(INTERNET_LAYER, "IcmpV6 (PacketTooBig)");
		} else if (packet != null
				&& packet.getClass() != IllegalPacket.class
				&& packet.getClass() != UnknownPacket.class
				&& packet.getClass() != FragmentedPacket.class
				&& packet.getClass() != IpV6ExtFragmentPacket.class) {
			System.out.println(packet.getClass());
		}

		if (packet != null) addAttributes(entry, packet.getPayload());
	}

	@Override
	public Attributes getFileAttributes() {
		return new Attributes();
	}

	@Override
	public byte[] getFileHeader() {
		return globalHeader;
	}

	@Override
	public void close() {
		LOG.info("closing " + file);

		pcap.close();

		if (tempFile != null) {
			tempFile.delete();
		}
	}

	private byte[] readGlobalHeader(File file) {
		try {
			byte[] buffer = new byte[24];
			if (file.getName().toLowerCase().endsWith(".gz")) {
				IOUtils.read(new GZIPInputStream(new FileInputStream(file)), buffer);
			} else {
				IOUtils.read(new FileInputStream(file), buffer);
			}

			return buffer;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
