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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import de.ecclesia.kipeto.common.util.Strings;

public class EntryUtil {

	private static Set<String> VALID_KEYS = new HashSet<String>(
			Arrays.asList(new String[] { "airplane", "alarm",
					"analytics|activity", "analytics|feedback", "app|XXX",
					"app|XXX|privatedirty", "app|XXX|Pss",
					"app|XXX|importance", "app|XXX|name",
					"app|XXX|privatedirty", "app|XXX|pss",
					"app|XXX|shareddirty", "app|XXX|starttime",
					"app|XXX|stime", "app|XXX|uTime", "app|XXX|utime",
					"app|datacleared", "app|end", "app|installed",
					"app|markets", "app|recent|XXX", "app|start",
					"app|updated", "audio|dates", "audio|headset|microphone",
					"audio|headset|present", "audio|headset|type",
					"audio|maxvolume|alarm", "audio|maxvolume|dtmf",
					"audio|maxvolume|music", "audio|maxvolume|notification",
					"audio|maxvolume|ring", "audio|maxvolume|system",
					"audio|maxvolume|voicecall", "audio|ringermode",
					"audio|volume|alarm", "audio|volume|dtmf",
					"audio|volume|music", "audio|volume|notification",
					"audio|volume|ring", "audio|volume|system",
					"audio|volume|voicecall", "audmo|volume|dtmf",
					"bluetooth|discovery", "bluetooth|found|XXX|bondstate",
					"bluetooth|found|XXX|class", "bluetooth|found|XXX|rssi",
					"bluetooth|scanmode", "bluetooth|state",
					"broken|app|installed", "broken|crash", "broken|log",
					"celllocation|cid", "celllocation|lac",
					"conn|XXX|detailedstate", "conn|XXX|state",
					"conn|XXX|subtype", "contacts", "cpu|XXX|timeinstates",
					"database1|tx", "discovery", "dock",
					"hashmapping|XXX|alias", "hashmapping|XXX|cleared",
					"hf|app", "hf|locked", "hf|net|XXX", "hf|net|app|XXX",
					"image|dates", "install", "location|latitude",
					"location|latlon", "location|longitude",
					"manualnetwork|3g", "manualnetwork|wifi",
					"memorycard|info|free", "memorycard|info|size",
					"memorycard|mounted", "memorycard|removed",
					"memorycard|shared", "memorycard|unmounted", "net|XXX|rx",
					"net|XXX|tx", "net|statistics|XXX", "net|total|rx_bytes",
					"net|total|rx_packets", "net|total|tx_bytes",
					"net|total|tx_packets", "net|mobile|rx_bytes",
					"net|mobile|rx_packets", "net|mobile|tx_bytes",
					"net|mobile|tx_packets", "net|app|XXX|rx_bytes",
					"net|app|XXX|rx_packets", "net|app|XXX|tx_bytes",
					"net|app|XXX|tx_packets", "nfc|enabled", "nfc|present",
					"pause", "pause|automatic", "phone|activeoperator",
					"phone|calling", "phone|celllocation|basestationid",
					"phone|celllocation|cid", "phone|celllocation|lac",
					"phone|celllocation|networkid", "phone|celllocation|psc",
					"phone|celllocation|systemid", "phone|idle",
					"phone|keyguardremoved", "phone|offhook", "phone|ringing",
					"phone|roaming", "phone|servicestate", "phone|signal|cdma",
					"phone|signal|gsm", "phone|sim|operator",
					"phone|sim|serial", "power|battery|health",
					"power|battery|level", "power|battery|plugged",
					"power|battery|present", "power|battery|scale",
					"power|battery|status", "power|battery|technology",
					"power|battery|temperature", "power|battery|voltage",
					"power|charger", "root|suBinary", "root|superuserApk",
					"root|testKeys", "screen|brightness|level",
					"screen|brightness|mode", "screen|power",
					"sensor|data|XXX|raw", "sensor|data|XXX|window",
					"sensor|info|XXX|maxRange", "sensor|info|XXX|minDelay",
					"sensor|info|XXX|name", "sensor|info|XXX|power",
					"sensor|info|XXX|resolution", "sensor|info|XXX|type",
					"sensor|info|XXX|vendor", "sensor|info|XXX|version",
					"shutdown", "sms|count|inbox", "sms|count|sent",
					"sms|count|unread", "sms|received", "sms|sent", "startup",
					"storage|additional|XXX", "storage|external",
					"storage|internal", "storage|lowmemory",
					"system|anykeyboardactive", "system|apiversion",
					"system|availablefeatures", "system|build|abi",
					"system|build|abi2", "system|build|board",
					"system|build|bootloader", "system|build|brand",
					"system|build|display", "system|build|fingerprint",
					"system|build|hardware", "system|build|host",
					"system|build|product", "system|build|radio",
					"system|build|serial", "system|build|tags",
					"system|build|time", "system|cpu|board",
					"system|cpu|bogomips", "system|cpu|cores",
					"system|cpu|maxfreq", "system|cpu|minfreq",
					"system|cpu|name", "system|device", "system|devs",
					"system|display|density", "system|display|dpi",
					"system|display|fontscale", "system|display|modetype",
					"system|display|night", "system|display|orientation",
					"system|display|resolution", "system|freeinternal",
					"system|gpu|name", "system|gpu|vendor",
					"system|hasvibrator", "system|hw|keyboard",
					"system|hw|keyboardhidden", "system|hw|nav",
					"system|hw|navhidden", "system|hw|touch", "system|kernel",
					"system|locale", "system|locale|current",
					"system|locale|default", "system|manufacturer",
					"system|memory|classlarge", "system|memory|classregular",
					"system|memory|free", "system|memory|size",
					"system|memory|threshold", "system|model",
					"system|osbuildtype", "system|osstring",
					"system|preference|allow3G",
					"system|preference|allowRoaming",
					"system|preference|compactRegularly",
					"system|preference|didAcceptTerms",
					"system|preference|monthStart",
					"system|preference|password",
					"system|preference|pollInterval",
					"system|preference|separateSim",
					"system|preference|sharingApps",
					"system|preference|sharingCamOnly",
					"system|preference|sharingGSM",
					"system|preference|sharingLocation",
					"system|preference|showIcon",
					"system|preference|studyCodes",
					"system|preference|usePolling", "system|preference|user",
					"system|settings|accessibility",
					"system|settings|accessibilityserv", "system|settings|adb",
					"system|settings|autotime", "system|settings|bgdata",
					"system|settings|dataroaming",
					"system|settings|hapticfeedback", "system|settings|lock",
					"system|settings|locktactile",
					"system|settings|lockvisible",
					"system|settings|nonmarketapps",
					"system|settings|parentalcontrol",
					"system|settings|screenoff",
					"system|settings|soundeffects", "system|settings|stayon",
					"system|settings|textautocaps",
					"system|settings|textautopunctuate",
					"system|settings|wifinotify", "system|settings|wifisleep",
					"system|store|free", "system|store|size", "system|swbuild",
					"system|swversion", "system|wallpaper", "tethering|active",
					"tethering|available", "tethering|errored", "time|bootup",
					"time|changed", "time|server", "upload", "video|dates",
					"wifi|connected|XXX|linkspeed", "wifi|connected|XXX|rssi",
					"wifi|connected|XXX|ssid", "wifi|scancomplete",
					"wifi|scan|XXX|capabilities", "wifi|scan|XXX|frequency",
					"wifi|scan|XXX|level", "wifi|scan|XXX|pss",
					"wifi|scan|XXX|ssid", "wifi|state" }));

	private static final String UNKNOWN = "UNKNOWN";
	private static final String INVALID = "INVALID";

	private static Pattern digitPattern = Pattern.compile("[0-9]+");
	private static Joiner pipeJoiner = Joiner.on("|");

	public static boolean isValid(String key) {
		return VALID_KEYS.contains(key);
	}

	public static String getDateFromLine(String line) {
		String[] parts = line.split(";");

		if (parts.length < 3 || parts[2].length() < 10) return INVALID;
		else return parts[2].substring(0, 10);
	}

	public static String getKeyFromLine(String line) {
		String[] parts = line.split(";");

		if (parts.length < 4) return UNKNOWN;

		String key = parts[3];
		String reducedKey = reduceKey(key);

		if (EntryUtil.isValid(reducedKey)) {
			return shortenKey(reducedKey);
		} else {
			System.out.println("invalid: " + reducedKey);
			return INVALID;
		}
	}

	public static String getValueFromLine(String line) {
		String[] parts = line.split(";");

		if (parts.length < 5) return UNKNOWN;
		else return parts[4];
	}

	private static String reduceKey(String key) {
		String[] splitKey = key.split("\\|");
		for (int i = 0; i < splitKey.length; ++i) {
			Matcher digitMatcher = digitPattern.matcher(splitKey[i]);
			if (splitKey[i].length() == 40 || digitMatcher.matches()) {
				splitKey[i] = "XXX";
			}
		}

		if (splitKey[0].equals("net") && !Strings.equalsAtLeastOne(splitKey[1], "statistics", "total", "mobile", "app")) {
			splitKey[1] = "XXX";
		}

		if (splitKey[0].equals("conn")) {
			splitKey[1] = "XXX";
		}

		if (splitKey[0].equals("net") && splitKey[1].equals("statistics")) {
			splitKey[2] = "XXX";
		}

		if (splitKey[0].equals("hf") && splitKey[1].equals("net")) {
			if (splitKey[2].equals("app")) {
				splitKey[3] = "XXX";
			}
			else {
				splitKey[2] = "XXX";
			}
		}

		return pipeJoiner.join(Arrays.asList(splitKey));
	}

	private static String shortenKey(String key) {
		// Limit keys to two level to avoid too much fragmentation
		return pipeJoiner.join(Arrays.asList(key.split("\\|")).stream().filter(s -> !s.equals("XXX")).limit(2).toArray());

	}

}
