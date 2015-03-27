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

// http://www.tcpdump.org/linktypes.html
public enum LinkType {

	LINKTYPE_NULL(0),
	LINKTYPE_ETHERNET(1),
	LINKTYPE_EXP_ETHERNET(2),
	LINKTYPE_AX25(3),
	LINKTYPE_PRONET(4),
	LINKTYPE_CHAOS(5),
	LINKTYPE_TOKEN_RING(6),
	LINKTYPE_ARCNET(7),
	LINKTYPE_SLIP(8),
	LINKTYPE_PPP(9),
	LINKTYPE_FDDI(10),
	LINKTYPE_PPP_HDLC(50),
	LINKTYPE_PPP_ETHER(51),
	LINKTYPE_ATM_RFC1483(100),
	LINKTYPE_RAW(101),
	LINKTYPE_C_HDLC(104),
	LINKTYPE_IEEE802_11(105),
	LINKTYPE_FRELAY(107),
	LINKTYPE_LOOP(108),
	LINKTYPE_LINUX_SLL(113),
	LINKTYPE_LTALK(104),
	LINKTYPE_PFLOG(117),
	LINKTYPE_PRISM_HEADER(119),
	LINKTYPE_IP_OVER_FC(122),
	LINKTYPE_SUNATM(123),
	LINKTYPE_IEEE802_11_RADIO(127),
	LINKTYPE_ARCNET_LINUX(129),
	LINKTYPE_APPLE_IP_OVER_IEEE1394(138),
	LINKTYPE_MTP2_WITH_PHDR(139),
	LINKTYPE_MTP2(140),
	LINKTYPE_MTP3(141),
	LINKTYPE_SCCP(142),
	LINKTYPE_DOCSIS(143),
	LINKTYPE_LINUX_IRDA(144),
	LINKTYPE_LINUX_LAPD(177),

	LINKTYPE_UNKNOWN(-1);

	private int id;

	private LinkType(int id) {
		this.id = id;
	}

	public static LinkType findById(int id) {
		for (LinkType type : LinkType.values()) {
			if (type.id == id) return type;
		}

		return LINKTYPE_UNKNOWN;
	}
}
