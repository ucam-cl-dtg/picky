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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import uk.ac.cam.cl.dtg.picky.util.IO;

import com.google.common.base.Preconditions;

import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.common.util.Strings;
import de.ecclesia.kipeto.compressor.Compressor;

public class Dataset extends PrecompressedGZIPBlob {

	private static final byte CURRENT_BINARY_VERSION = 1;
	private static final String NULL = "NULL";

	private String description;
	private String url;
	private byte[] icon;

	private List<FileEntry> files;

	private String cachedContentDescription;

	public Dataset(String id, String compression, InputStream contentStream, Long contentLength) {
		super(id, contentStream, contentLength);

		IO.wrap(() -> {
			DataInputStream inputStream = new DataInputStream(contentStream);
			byte binaryVersion = inputStream.readByte();

			Preconditions.checkArgument(binaryVersion == CURRENT_BINARY_VERSION, "Unsupported binary version <" + binaryVersion + ">");

			description = inputStream.readUTF();
			if (NULL.equals(description)) description = null;

			url = inputStream.readUTF();
			if (NULL.equals(url)) url = null;

			int iconBytes = inputStream.readInt();
			icon = new byte[iconBytes];
			inputStream.readFully(icon);

			files = new ArrayList<>();
			Optional<FileEntry> entry;

			while ((entry = FileEntry.fromStream(inputStream)).isPresent()) {
				files.add(entry.get());
			}
		});
	}

	private Dataset(byte[] content) {
		super(Compressor.NONE, new ByteArrayInputStream(content), (long) content.length);
	}

	public List<FileEntry> getFiles() {
		return files;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public byte[] getIcon() {
		return icon;
	}

	public String getContentDescription() {
		if (cachedContentDescription == null) {
			NumberFormat numberFormat = NumberFormat.getNumberInstance();

			int numberOfFiles = files.size();
			long size = files.stream()
					.flatMap(f -> f.getBlocks().stream())
					.flatMap(b -> b.getChunks().stream())
					.mapToLong(c -> c.getLengthUncompressed()).sum();

			cachedContentDescription = numberFormat.format(numberOfFiles) + " files (" + FileSizeFormatter.formateBytes(size, 1) + ")";
		}

		return cachedContentDescription;
	}

	public String getId() {
		return super.id();
	}

	public static class DatasetBuilder {

		private ByteArrayOutputStream arrayOutputStream;
		private GZIPOutputStream compressedOutputStream;
		private DataOutputStream dataOutputStream;
		private String description;
		private String url;
		private byte[] icon;

		public DatasetBuilder(String description, String url, byte[] icon) {
			this.description = description;
			this.url = url;
			this.icon = icon != null ? icon : new byte[] {};

			IO.wrap(() -> {
				arrayOutputStream = new ByteArrayOutputStream();
				compressedOutputStream = new GZIPOutputStream(arrayOutputStream);
				dataOutputStream = new DataOutputStream(compressedOutputStream);

				dataOutputStream.writeByte(CURRENT_BINARY_VERSION);
				dataOutputStream.writeUTF(Strings.firstNonNull(description, NULL));
				dataOutputStream.writeUTF(Strings.firstNonNull(url, NULL));
				dataOutputStream.writeInt(icon.length);
				dataOutputStream.write(icon);
			});
		}

		public synchronized void appendFile(FileEntry entry) {
			entry.writeToStream(dataOutputStream);
		}

		public Dataset build() {
			FileEntry.writeEOF(dataOutputStream);

			IO.wrap(() -> {
				dataOutputStream.flush();
				compressedOutputStream.flush();
				compressedOutputStream.finish();
				arrayOutputStream.flush();
			});

			Dataset dataset = new Dataset(arrayOutputStream.toByteArray());
			dataset.description = description;
			dataset.url = url;
			dataset.icon = icon;

			return dataset;
		}

	}

}
