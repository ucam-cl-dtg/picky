package uk.ac.cam.cl.dtg.picky.parser.cluster;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.parser.Entry;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

public class ClusterParser implements IEntryParser {

	private static final String EVENT_TYPE = "EVENT_TYPE";
	private static final String KIND = "KIND";

	private static final Logger LOG = LoggerFactory.getLogger(ClusterParser.class);
	private GZIPInputStream stream;
	private BufferedReader reader;

	private Attributes fileAttributes = new Attributes();

	@Override
	public void open(File file) throws IOException {
		stream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
		reader = new BufferedReader(new InputStreamReader(stream));

		fileAttributes.put(KIND, file.getParentFile().getName());
	}

	@Override
	public Optional<Entry> readEntry() {
		try {
			String line = reader.readLine();

			if (line == null) {
				return Optional.empty();
			} else {
				Entry entry = new Entry(line.getBytes());

				String[] parts = line.split(",");
				if ("task_events".equals(fileAttributes.get(KIND))) {
					entry.getAttributes().put(EVENT_TYPE, getEventType(parts[5]));
				} else if ("job_events".equals(fileAttributes.get(KIND))) {
					entry.getAttributes().put(EVENT_TYPE, getEventType(parts[3]));
				} else if ("task_constraints".equals(fileAttributes.get(KIND))) {
					entry.getAttributes().put(EVENT_TYPE, getConstraintType(parts[3]));
				}

				return Optional.of(entry);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getConstraintType(String type) {
		switch (Integer.parseInt(type)) {
		case 0:
			return "EQUAL (0)";
		case 1:
			return "NOT EQUAL (1)";
		case 2:
			return "LESS THAN (2)";
		case 3:
			return " GREATER THAN (3)";
		default:
			return "UNKNOWN";
		}
	}

	private String getEventType(String type) {
		switch (Integer.parseInt(type)) {
		case 0:
			return "SUBMIT (0)";
		case 1:
			return "SCHEDULE (1)";
		case 2:
			return "EVICT (2)";
		case 3:
			return "FAIL (3)";
		case 4:
			return "FINISH (4)";
		case 5:
			return "KILL (5)";
		case 6:
			return "LOST (6)";
		case 7:
			return "UPDATE_PENDING (7)";
		case 8:
			return "UPDATE_RUNNING (8)";
		default:
			return "UNKNOWN";
		}
	}

	@Override
	public Attributes getFileAttributes() {
		return fileAttributes;
	}

	@Override
	public byte[] getFileHeader() {
		return new byte[] {};
	}

	@Override
	public void close() {
		try {
			if (stream != null) {
				stream.close();
			}

			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
