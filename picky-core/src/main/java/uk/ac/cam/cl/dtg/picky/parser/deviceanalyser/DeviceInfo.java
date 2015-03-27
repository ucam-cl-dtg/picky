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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceInfo {
	public String device;
	public String locale;
	public String manufacturer;
	public String osbuildtype;
	public String osstring;
	public String apiversion;
	public String devicemodel;
	public Date starttime;
	public Date endtime;
	public Boolean suBinary;
	public Boolean superuserApk;
	public Boolean testKeys;
	public String resolution;
	public String dpi;

	public AtomicInteger rooted_true_count = new AtomicInteger(0);
	public AtomicInteger rooted_false_count = new AtomicInteger(0);

	public Boolean getRooted() {
		if (suBinary == null && superuserApk == null && testKeys == null) return null;
		if (suBinary == Boolean.TRUE) return true;
		if (superuserApk == Boolean.TRUE) return true;
		if (testKeys == Boolean.TRUE) return true;

		return false;
	}

	public String getCountry() {
		if (locale == null) return null;

		String[] parts = locale.split("_");
		return new Locale(parts[0], parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "").getDisplayCountry();
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		else return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
