/*
 * Copyright 2012 Olhe Ingenieros SL.
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
package com.olheingenieros.listexample;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.olheingenieros.listexample.provider.TutListSharedPrefs;
import com.olheingenieros.listexample.sync.TriggerSyncReceiver;

/**
 * @author olheingenierossl
 *
 */
public class TutListPreferencesActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(
                TutListSharedPrefs.PREFS_NAME);
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final Context context = getApplicationContext();
        if (TutListSharedPrefs.getBackgroundUpdateFlag(getApplicationContext())) {
            TriggerSyncReceiver.setRecurringAlarm(context);
        } else {
            TriggerSyncReceiver.cancelRecurringAlarm(context);
        }
    }

}
