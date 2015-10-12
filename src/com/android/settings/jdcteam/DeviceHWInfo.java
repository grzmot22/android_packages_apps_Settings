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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.android.settings.R;

/**
 * Created by antaresone on 10/10/15.
 *
 * TODO:
 * - realtime CPU frequency scaling
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
	private static final String PROPERTY_CPU_FREQUENCY = "ro.device.cpu.frequency"; // set this with a value (example: ro.device.cpu.frequency=1890 MHz to show "Frequency 1890 MHz") for fixed maximum frequency
	private static final String PROPERTY_CPU_REALTIME_FREQUENCY = "ro.device.cpu.realtime_freq"; // set this to true for main core (0) realtime maximum frequency only
	private static final String PROPERTY_CPU_REALTIME_FREQUENCY_ALLCORES= "ro.device.cpu.realtime_freq.all"; // set this to true for all cores realtime maximum frequency
	private static final String PROPERTY_GPU_MODEL = "ro.device.gpu.model";
	private static final String PROPERTY_GPU_FREQUENCY = "ro.device.gpu.frequency"; // set this with a value (example: ro.device.gpu.frequency=450 MHz to show "Frequency 450 MHz") for fixed maximum frequency
	private static final String PROPERTY_GPU_REALTIME_FREQUENCY = "ro.device.gpu.realtime_freq"; // set this to true for GPU realtime maximum frequency
	private static final String PROPERTY_RAM_INFO = "ro.device.show_ram_info";
	
	// CPU frequency from sysfs
	private static final String SYSFS_CPUFREQ_CORE_0 = "/sys/devices/system/cpu/cpufreq/all_cpus/scaling_max_freq_cpu0";
	private static final String SYSFS_CPUFREQ_CORE_1 = "/sys/devices/system/cpu/cpufreq/all_cpus/scaling_max_freq_cpu1";
	private static final String SYSFS_CPUFREQ_CORE_2 = "/sys/devices/system/cpu/cpufreq/all_cpus/scaling_max_freq_cpu2";
	private static final String SYSFS_CPUFREQ_CORE_3 = "/sys/devices/system/cpu/cpufreq/all_cpus/scaling_max_freq_cpu3";
	private static final String SYSFS_GPUFREQ = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
	
	// Megabyte, Megahertz, newline
	private static final String MB = " MB";
	private static final String MHZ = " MHz";
	private static final String NL = "\n";
	
	// Initialize these bools as false
	private boolean isAllCores = false;
	private boolean isGpu = false;
	
	// Updated by getFreqFromSysFs string below
	private File coreNum;

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
		
		// Remove CPU cores count info if property is missing
		removePreference(getPreferenceScreen(), KEY_CPU_COUNT,
		PROPERTY_CPU_COUNT);
		
		// Remove CPU frequency info if property is missing
		if (!cpuRealtimeFreq()) {
			removePreference(getPreferenceScreen(), KEY_CPU_FREQUENCY,
			PROPERTY_CPU_FREQUENCY);
		} else if (!cpuRealtimeFreq() && !isAllCores) {
			removePreference(getPreferenceScreen(), KEY_CPU_FREQUENCY,
			PROPERTY_CPU_REALTIME_FREQUENCY_ALLCORES);
		} else {
			removePreference(getPreferenceScreen(), KEY_CPU_FREQUENCY,
			PROPERTY_CPU_REALTIME_FREQUENCY);
		}
		
		// Remove GPU model info if property is missing
		removePreference(getPreferenceScreen(), KEY_GPU_MODEL,
		PROPERTY_GPU_MODEL);
		
		// Remove GPU frequency info if property is missing
		if (!gpuRealtimeFreq()) {
			removePreference(getPreferenceScreen(), KEY_GPU_FREQUENCY,
			PROPERTY_GPU_REALTIME_FREQUENCY);
		} else {
			removePreference(getPreferenceScreen(), KEY_GPU_FREQUENCY,
			PROPERTY_GPU_FREQUENCY);
		}
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
		if (!cpuRealtimeFreq()) {
		setFromBuildProp(KEY_CPU_FREQUENCY, PROPERTY_CPU_FREQUENCY);
		} else if (cpuRealtimeFreq() && isAllCores) {
			setFreqFromSysFs(1);
		} else {
			setFreqFromSysFs(0);
		}
		setFromBuildProp(KEY_GPU_MODEL, PROPERTY_GPU_MODEL);
		if (gpuRealtimeFreq()) {
			setFromString(KEY_GPU_FREQUENCY, getFreqFromSysFs(4));
		} else {
			setFromBuildProp(KEY_GPU_FREQUENCY, PROPERTY_GPU_FREQUENCY);
		}
		setFromString(KEY_RAM_INFO, totalMemory + usedMemory + freeMemory);
	}
	
	private void removePreference(PreferenceGroup preferenceGroup,
		String preference, String property ) {
		if (SystemProperties.get(property).equals("") ) {
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
	
	private boolean cpuRealtimeFreq() {
		if (SystemProperties.get(PROPERTY_CPU_REALTIME_FREQUENCY).equals("true")) {
			return true;
		} else if (SystemProperties.get(PROPERTY_CPU_REALTIME_FREQUENCY_ALLCORES).equals("true") | SystemProperties.get(PROPERTY_CPU_REALTIME_FREQUENCY).equals("true") && SystemProperties.get(PROPERTY_CPU_REALTIME_FREQUENCY_ALLCORES).equals("true")) {
			// Handle both "if only ALLCORES" and "if realtime frequency and ALLCORES" in the same way
			isAllCores = true;
			return true;
		} else {
		return false;
		}
	}
	
	private boolean gpuRealtimeFreq() {
		if (SystemProperties.get(PROPERTY_GPU_REALTIME_FREQUENCY).equals("true")) {
			return true;
		}
		return false;
	}
	
	private void setFreqFromSysFs(int allCores) {
		Preference cpuFreq = findPreference(KEY_CPU_FREQUENCY);
		switch (allCores) {
			case 0:
				cpuFreq.setSummary(getFreqFromSysFs(0));
				break;
			case 1:
				// Handle newlines here, 'cause if put in tuneValue string will put a space below text
				cpuFreq.setSummary(getString(R.string.device_cpufreq_core0) + ": " + getFreqFromSysFs(0) + NL + getString(R.string.device_cpufreq_core1) + ": " + getFreqFromSysFs(1) + NL + getString(R.string.device_cpufreq_core2) + ": " + getFreqFromSysFs(2) + NL + getString(R.string.device_cpufreq_core3) + ": " + getFreqFromSysFs(3));
				break;
		}
	}
	
	private String getFreqFromSysFs(int whichCore) {
		String sysFsItem = null;
		String coreFreq = null;
		switch (whichCore) {
		case 0:
			coreNum = new File(SYSFS_CPUFREQ_CORE_0);
			break;
		case 1:
			coreNum = new File(SYSFS_CPUFREQ_CORE_1);
			break;
		case 2:
			coreNum = new File(SYSFS_CPUFREQ_CORE_2);
			break;
		case 3:
			coreNum = new File(SYSFS_CPUFREQ_CORE_3);
			break;
		case 4:
			// Handle GPU frequency here
			isGpu = true;
			coreNum = new File(SYSFS_GPUFREQ);
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(coreNum);
			sysFsItem = streamToString(fileInputStream);
			coreFreq = tuneValue(sysFsItem);
			fileInputStream.close();
		} catch (Exception e) {
			// Enjoy the silence
		}
		return coreFreq;
	}

	private static String streamToString(InputStream inputStream) throws Exception {
		// Read the given file
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String value = null;
		while ((value = bufferedReader.readLine()) != null) {
			stringBuilder.append(value);
		}
		bufferedReader.close();
		return stringBuilder.toString();
	}
	
	private String tuneValue(String valueToFix) {
		String finalValue = null;
		float currValue = Float.parseFloat(valueToFix);
		if (!isGpu) {
			currValue = Math.round(currValue / 1000);
		} else {
			// GPU frequency needs to be handled differently
			currValue = Math.round(currValue / (1000*1000));
		}
		finalValue = Float.toString(currValue).split("\\.")[0] + MHZ;
		return finalValue;
	}
}
