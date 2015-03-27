package uk.ac.cam.cl.dtg.picky.parser;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.cam.cl.dtg.picky.util.Loop;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;

@SuppressWarnings("serial")
public class Attributes extends HashMap<String, Object> {

	private static final byte CURRENT_BINARY_VERSION = 1;

	public void writeToStream(DataOutputStream dataOutputStream) throws IOException {
		// Remove null values
		Set<String> keysToRemove = keySet().stream().filter((k) -> get(k) == null).collect(Collectors.toSet());
		keysToRemove.forEach(this::remove);

		dataOutputStream.writeByte(CURRENT_BINARY_VERSION);
		dataOutputStream.writeInt(size());

		for (String key : keySet()) {
			Object object = get(key);
			dataOutputStream.writeUTF(key);
			dataOutputStream.writeUTF(object.getClass().getSimpleName());

			if (object instanceof Integer) {
				dataOutputStream.writeInt((Integer) object);
			} else if (object instanceof Double) {
				dataOutputStream.writeDouble((Double) object);
			} else if (object instanceof Long) {
				dataOutputStream.writeLong((Long) object);
			} else if (object instanceof Float) {
				dataOutputStream.writeFloat((Float) object);
			} else if (object instanceof Boolean) {
				dataOutputStream.writeBoolean((Boolean) object);
			} else if (object instanceof String) {
				dataOutputStream.writeUTF((String) object);
			} else {
				throw new UnsupportedOperationException("Class " + object.getClass() + " not supported");
			}
		}
	}

	public static Attributes fromStream(DataInputStream inputStream) throws IOException {
		byte binaryVersion = inputStream.readByte();

		Preconditions.checkArgument(binaryVersion == CURRENT_BINARY_VERSION, "Unsupported binary version <" + binaryVersion + ">");

		Attributes attributes = new Attributes();
		int size = inputStream.readInt();
		Loop.doFor(() -> size, () -> {
			String key = inputStream.readUTF();
			String clazz = inputStream.readUTF();
			Object value;

			if (Integer.class.getSimpleName().equals(clazz)) {
				value = inputStream.readInt();
			} else if (Double.class.getSimpleName().equals(clazz)) {
				value = inputStream.readDouble();
			} else if (Long.class.getSimpleName().equals(clazz)) {
				value = inputStream.readLong();
			} else if (Float.class.getSimpleName().equals(clazz)) {
				value = inputStream.readFloat();
			} else if (Boolean.class.getSimpleName().equals(clazz)) {
				value = inputStream.readBoolean();
			} else if (String.class.getSimpleName().equals(clazz)) {
				value = inputStream.readUTF();
			} else {
				throw new UnsupportedOperationException("Class " + clazz + " not supported");
			}

			attributes.put(key, value);
		});

		return attributes;
	}

	public static boolean haveCommonValues(Attributes a, ListMultimap<String, String> b) {
		HashSet<String> commonKeys = new HashSet<>(a.keySet());
		commonKeys.retainAll(b.keySet());

		for (String key : commonKeys) {
			if (b.get(key).contains(a.get(key))) return true;
		}

		return false;
	}

	public static boolean haveCommonValues(ListMultimap<String, String> a, ListMultimap<String, String> b) {
		HashSet<String> commonKeys = new HashSet<>(a.keySet());
		commonKeys.retainAll(b.keySet());

		for (String key : commonKeys) {
			if (!Collections.disjoint(a.get(key), b.get(key))) return true;
		}

		return false;
	}
}
