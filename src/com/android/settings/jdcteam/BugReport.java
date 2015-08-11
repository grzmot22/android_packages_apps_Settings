package com.android.settings.jdcteam;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.widget.Toast;

import com.android.settings.R;

/**
 * Created by antaresone on 23/07/15.
 */
public class BugReport extends PreferenceActivity {

    int taps = 0;
    Intent start;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bug_report_prefs);
        Preference mss = findPreference ("mss");
        mss.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                taps = taps+1;
                switch ( taps ) {
                    case 7:
                        toast = new Toast(BugReport.this);
                        toast.makeText(BugReport.this, R.string.missing_clicks, Toast.LENGTH_SHORT).show();
                        break;
                    case 10:
                        start = new Intent (BugReport.this, WeirdShit.class);
                        startActivity(start);
                        break;
                }
                return false;
            }
        });
    }
}
