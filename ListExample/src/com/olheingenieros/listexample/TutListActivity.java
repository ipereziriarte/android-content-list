/*
 * Copyright (c) 2011, Lauren Darcey and Shane Conder
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of
 *   conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice, this list
 *   of conditions and the following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 * * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific prior
 *   written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * <ORGANIZATION> = Mamlambo
 */

package com.olheingenieros.listexample;

import static com.olheingenieros.listexample.utils.LogUtils.LOGI;
import static com.olheingenieros.listexample.utils.LogUtils.isLogable;
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TutListActivity extends SherlockFragmentActivity implements
TutListFragment.OnTutSelectedListener {

    private static final String TAG = makeLogTag(TutListActivity.class);
    private static final Boolean DEVELOPER_MODE = isLogable();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork() // or .detectAll() for all detectable
            // problems
            .penaltyLog()
            .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .penaltyDeath()
            .build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutlist_fragment);
        LOGI(TAG, "Activity Created");

    }

    @Override
    public void onTutSelected(final String tutUrl) {
        final TutViewerFragment viewer = (TutViewerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.tutview_fragment);

        if (viewer == null || !viewer.isInLayout()) {
            final Intent showContent = new Intent(getApplicationContext(),
                    TutViewerActivity.class);
            showContent.setData(Uri.parse(tutUrl));
            startActivity(showContent);
        } else {
            viewer.updateUrl(tutUrl);
        }
    }
}