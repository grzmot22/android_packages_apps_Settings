/*
 * Copyright (C) 2015 The JDCTeam
 *
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
 */

package com.android.settings.jdcteam;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.Log;

import com.android.settings.R;

/**
 * Created by antaresone on 10/10/15.
 */
public class DeviceHWInfo extends PreferenceActivity {

	// Activity name in log
	private static final String ACTIVITY_TAG = "JDC-DeviceHWInfo";
	
	// Keys from device_hardware_info.xml
	private static final String KEY_SOC_MANUFACTURER = "device_soc_manufacturer";
	private static final String KEY_SOC_MODEL = "device_soc_model";
	private static final String KEY_CPU_COUNT = "device_cpu_count";
	private static final String KEY_CPU_FREQUENCY = "device_cpu_frequency";
	private static final String KEY_GPU_MODEL = "device_gpu_model";
	private static final String KEY_GPU_FREQUENCY = "device_gpu_frequency";
	private static final String KEY_RAM_INFO = "device_ram_info";
	
	// Properties from build.prop
	private static final String PROPERTY_SOC_MANUFACTURER = "ro.device.soc.manufacturer";
	private static final String PROPERTY_SOC_MODEL = "ro.device.soc.model";
	private static final String PROPERTY_CPU_COUNT = "ro.device.cpu.count";
	private static final String PROPERTY_CPU_FREQUENCY = "ro.device.cpu.frequency";
	private static final String PROPERTY_GPU_MODEL = "ro.device.gpu.model";
	private static final String PROPERTY_GPU_FREQUENCY = "ro.device.gpu.frequency";
	private static final String PROPERTY_RAM_INFO = "ro.device.show_ram_info";
	
	// Megabyte, newline
	private static final String MB = " MB";
	private static final String NL = "\n";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.device_hardware_info);
		
		// Remove SoC manufacturer info if property is missing
		removePreference(getPreferenceScreen(), KEY_SOC_MANUFACTURER,
		PROPERTY_SOC_MANUFACTURER);
		
		// Remove SoC model info if property is missing
		removePreference(getPreferenceScreen(), KEY_SOC_MODEL,
		PROPERTY_SOC_MODEL);
		
		// Remove CPU cores count info info if property is missing
		removePreference(getPreferenceScreen(), KEY_CPU_COUNT,
		PROPERTY_CPU_COUNT);
		
		// Remove CPU frequency info info if property is missing
		removePreference(getPreferenceScreen(), KEY_CPU_FREQUENCY,
		PROPERTY_CPU_FREQUENCY);
		
		// Remove GPU model info if property is missing
		removePreference(getPreferenceScreen(), KEY_GPU_MODEL,
		PROPERTY_GPU_MODEL);
		
		// Remove GPU frequency info if property is missing
		removePreference(getPreferenceScreen(), KEY_GPU_FREQUENCY,
		PROPERTY_GPU_FREQUENCY);
		
		// Remove RAM info if property is missing
		removePreference(getPreferenceScreen(), KEY_RAM_INFO,
		PROPERTY_RAM_INFO);
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
	
		// Get RAM info in bytes
		long total = memoryInfo.totalMem;
		long free = memoryInfo.availMem;
		// Total RAM in Megabytes
		float floatTotal = Math.round(total / (1024 * 1024));
		// Available RAM in Megabytes
		float floatFree = Math.round(free / (1024 * 1024));
		// Used RAM in Megabytes
		float floatUsed = Math.round(floatTotal - floatFree);
		// Convert floats to strings
		String totalMemory = Float.toString(floatTotal);
		String usedMemory = Float.toString(floatUsed);
		String freeMemory = Float.toString(floatFree);
		// String totalMemory always ends with 0
		while (totalMemory.endsWith("0")) {
			totalMemory = getString(R.string.device_ram_size) + ": " + totalMemory.split("\\.")[0] + MB + NL;
			usedMemory = getString(R.string.device_ram_used) + ": " + usedMemory.split("\\.")[0] + MB + NL;
			freeMemory = getString(R.string.device_ram_free) + ": " + freeMemory.split("\\.")[0] + MB;
		}
		
		setFromBuildProp(KEY_SOC_MANUFACTURER, PROPERTY_SOC_MANUFACTURER);
		setFromBuildProp(KEY_SOC_MODEL, PROPERTY_SOC_MODEL);
		setFromBuildProp(KEY_CPU_COUNT, PROPERTY_CPU_COUNT);
		setFromBuildProp(KEY_CPU_FREQUENCY, PROPERTY_CPU_FREQUENCY);
		setFromBuildProp(KEY_GPU_MODEL, PROPERTY_GPU_MODEL);
		setFromBuildProp(KEY_GPU_FREQUENCY, PROPERTY_GPU_FREQUENCY);
		setFromString(KEY_RAM_INFO, totalMemory + usedMemory + freeMemory);
	}
	
	private void removePreference(PreferenceGroup preferenceGroup,
		String preference, String property ) {
		if (SystemProperties.get(property).equals("")) {
		// Property missing, remove preference
		try {
			preferenceGroup.removePreference(findPreference(preference));
		} catch (RuntimeException e) {
			Log.d(ACTIVITY_TAG, "Preference '" + preference + "' has been removed because property '"
				+ property + "' is missing");
		    }
		}
	}
	
	private void setFromBuildProp(String preference, String property) {
		try {
			// Property found, set summary
			findPreference(preference).setSummary(SystemProperties.get(property));
		} catch (RuntimeException e) {
			// Property not found, set null
			findPreference(preference).setSummary("N/A");
		}
	}
	
	private void setFromString(String preference, String value) {
		try {
			// Property found, set summary
			findPreference(preference).setSummary(value);
		} catch (RuntimeException e) {
			// Property not found, set null
			findPreference(preference).setSummary("N/A");
		}
	}
}
