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

import java.util.Date;

public class PackageHeader {

	/* timestamp seconds */
	private long ts_sec;

	/* timestamp microseconds */
	private long ts_usec;

	/* number of octets of packet saved in file */
	private long incl_len;

	/* actual length of packet */
	private long orig_len;

	private byte[] header;

	public PackageHeader(GlobalHeader globalHeader, byte[] header) {
		this.header = header;
		this.ts_sec = ByteUtil.readInt(header, 4, 0, globalHeader.isByteOrderSwapped());
		this.ts_usec = ByteUtil.readInt(header, 4, 4, globalHeader.isByteOrderSwapped());
		this.incl_len = ByteUtil.readInt(header, 4, 8, globalHeader.isByteOrderSwapped());
		this.orig_len = ByteUtil.readInt(header, 4, 12, globalHeader.isByteOrderSwapped());
	}

	public int getLength() {
		return (int) incl_len;
	}

	public int getOriginalLength() {
		return (int) orig_len;
	}

	public byte[] getHeader() {
		return header;
	}

	@Override
	public String toString() {
		return "PackageHeader [ts_sec=" + new Date(ts_sec * 1000) + ", ts_usec=" + ts_usec + ", incl_len=" + incl_len + ", orig_len="
				+ orig_len + "]";
	}
}
