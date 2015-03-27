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

public class ByteUtil {

	public static long readInt(byte[] data, int size, int offset, boolean swapped) {
		long result = 0;

		if (swapped) {
			for (int i = 0; i < size; i++) {
				result |= (data[offset + i] & 0xFF) << 8 * i;
			}
		} else {
			for (int i = size - 1; i >= 0; i--) {
				result |= (data[offset + i] & 0xFF) << 8 * (size - 1 - i);
			}
		}

		return result;
	}
}
