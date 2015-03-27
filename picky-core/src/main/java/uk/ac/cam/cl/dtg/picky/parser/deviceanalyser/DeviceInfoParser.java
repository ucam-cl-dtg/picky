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

import java.util.Arrays;
import java.util.List;

public class DeviceInfoParser {

	private static final List<String> EVENT_KEYS = Arrays.asList(
			"root|suBinary",
			"root|superuserApk",
			"root|testKeys",
			"system|apiversion",
			"system|display|resolution",
			"system|display|dpi",
			"system|manufacturer",
			"system|locale",
			"system|device");

	private DeviceInfo deviceInfo = new DeviceInfo();

	public void parse(String key, String value) {
		if (EVENT_KEYS.stream().anyMatch(k -> k.equals(key))) {
			parseEventForDeviceInfos(key, value);
		}
	}

	private void parseEventForDeviceInfos(String key, String value) {
		try {
			if ("system|display|dpi".equals(key)) deviceInfo.dpi = value;
			if ("system|display|resolution".equals(key)) deviceInfo.resolution = value;
			if ("root|suBinary".equals(key)) deviceInfo.suBinary = Boolean.parseBoolean(value);
			if ("root|superuserApk".equals(key)) deviceInfo.superuserApk = Boolean.parseBoolean(value);
			if ("root|testKeys".equals(key)) deviceInfo.testKeys = Boolean.parseBoolean(value);
			if ("system|apiversion".equals(key)) deviceInfo.apiversion = value;
			if ("system|osbuildtype".equals(key)) deviceInfo.osbuildtype = value;
			if ("system|osstring".equals(key)) deviceInfo.osstring = value;
			if ("system|manufacturer".equals(key)) deviceInfo.manufacturer = value;
			if ("system|locale".equals(key)) deviceInfo.locale = value;
			if ("system|device".equals(key)) deviceInfo.device = value;

			if (key.startsWith("root") && deviceInfo.getRooted() == Boolean.TRUE) deviceInfo.rooted_true_count.incrementAndGet();
			if (key.startsWith("root") && deviceInfo.getRooted() == Boolean.FALSE) deviceInfo.rooted_false_count.incrementAndGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

}
