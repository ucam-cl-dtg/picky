package uk.ac.cam.cl.dtg.picky.parser.deviceanalyser;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.parser.Attributes;
import uk.ac.cam.cl.dtg.picky.parser.Entry;
import uk.ac.cam.cl.dtg.picky.parser.IEntryParser;

import com.google.common.collect.Iterables;

public class DeviceAnalyserEntryParser implements IEntryParser {

	private static final Logger LOG = LoggerFactory.getLogger(DeviceAnalyserEntryParser.class);

	private static final String ATTRIBUTE_KEY = "key";
	private static final String ATTRIBUTE_DAYS = "days";
	private static final String ATTRIBUTE_FIRST_DAY = "first_day";
	private static final String ATTRIBUTE_LAST_DAY = "last_day";

	private static final String ATTRIBUTE_DEVICE = "device";
	private static final String ATTRIBUTE_LOCALE = "locale";
	private static final String ATTRIBUTE_LOCALE_COUNTRY = "locale_country";
	private static final String ATTRIBUTE_MANUFACTURER = "manufacturer";
	private static final String ATTRIBUTE_OSBUILDTYPE = "osbuildtype";
	private static final String ATTRIBUTE_OSSTRING = "osstring";
	private static final String ATTRIBUTE_APIVERSION = "apiversion";
	private static final String ATTRIBUTE_DEVICEMODEL = "devicemodel";
	private static final String ATTRIBUTE_RESOLUTION = "resolution";
	private static final String ATTRIBUTE_DPI = "dpi";
	private static final String ATTRIBUTE_ROOTED = "rooted";

	private BufferedReader reader;

	private Set<String> days = new HashSet<String>();

	private DeviceInfoParser deviceInfoParser;

	@Override
	public void open(InputStream inputStream) {
		reader = new BufferedReader(new InputStreamReader(inputStream));
		deviceInfoParser = new DeviceInfoParser();
	}

	@Override
	public Optional<Entry> readEntry() {
		String line;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (line == null) {
			return Optional.empty();
		} else {
			Entry entry = new Entry((line + "\n").getBytes());
			String key = EntryUtil.getKeyFromLine(line);
			String value = EntryUtil.getValueFromLine(line);

			deviceInfoParser.parse(key, value);

			entry.getAttributes().put(ATTRIBUTE_KEY, key);

			days.add(EntryUtil.getDateFromLine(line));

			return Optional.of(entry);
		}
	}

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public Attributes getFileAttributes() {
		Attributes attributes = new Attributes();

		List<String> daysSorted = days
				.stream()
				.filter(s -> s.matches("\\d{4}-\\d{2}-\\d{2}"))
				.sorted()
				.collect(Collectors.toList());

		attributes.put(ATTRIBUTE_DAYS, daysSorted.size());

		if (!daysSorted.isEmpty()) {
			attributes.put(ATTRIBUTE_FIRST_DAY, Iterables.getFirst(daysSorted, null));
			attributes.put(ATTRIBUTE_LAST_DAY, Iterables.getLast(daysSorted, null));
		}

		DeviceInfo deviceInfo = deviceInfoParser.getDeviceInfo();
		if (deviceInfo.apiversion != null) attributes.put(ATTRIBUTE_APIVERSION, deviceInfo.apiversion);
		if (deviceInfo.device != null) attributes.put(ATTRIBUTE_DEVICE, deviceInfo.device);
		if (deviceInfo.devicemodel != null) attributes.put(ATTRIBUTE_DEVICEMODEL, deviceInfo.devicemodel);
		if (deviceInfo.dpi != null) attributes.put(ATTRIBUTE_DPI, deviceInfo.dpi);
		if (deviceInfo.locale != null) attributes.put(ATTRIBUTE_LOCALE, deviceInfo.locale);
		if (deviceInfo.getCountry() != null) attributes.put(ATTRIBUTE_LOCALE_COUNTRY, deviceInfo.getCountry());
		if (deviceInfo.manufacturer != null) attributes.put(ATTRIBUTE_MANUFACTURER, deviceInfo.manufacturer);
		if (deviceInfo.osbuildtype != null) attributes.put(ATTRIBUTE_OSBUILDTYPE, deviceInfo.osbuildtype);
		if (deviceInfo.osstring != null) attributes.put(ATTRIBUTE_OSSTRING, deviceInfo.osstring);
		if (deviceInfo.resolution != null) attributes.put(ATTRIBUTE_RESOLUTION, deviceInfo.resolution);
		attributes.put(ATTRIBUTE_ROOTED, deviceInfo.getRooted());

		return attributes;
	}

	@Override
	public byte[] getFileHeader() {
		return new byte[0];
	}

}
