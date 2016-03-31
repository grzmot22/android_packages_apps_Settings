package com.android.settings;

import android.os.Bundle;
import com.android.internal.logging.MetricsLogger;

public class OptCMSettings extends SettingsPreferenceFragment {
		
		@Override
		protected int getMetricsCategory() {
			return MetricsLogger.DEVICEINFO;
		}
		
		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			addPreferencesFromResource(R.xml.optcm_settings);
			}
}	