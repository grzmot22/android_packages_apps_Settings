package com.android.settings.jdcteam;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.android.settings.R;

/**
 * Created by antaresone on 23/07/15.
 */
public class BugReport extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bug_report_prefs);
    }
}
