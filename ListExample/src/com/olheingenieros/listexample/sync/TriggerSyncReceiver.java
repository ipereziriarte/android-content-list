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
package com.olheingenieros.listexample.sync;

import static com.olheingenieros.listexample.utils.LogUtils.LOGD;
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;



/**
 * A simple {@link BroadCastReceiver} that triggers a sync.
 * 
 * @author olheingenierossl
 * @since  1.0
 */
public class TriggerSyncReceiver extends BroadcastReceiver {

    private static final String TAG = makeLogTag(TriggerSyncReceiver.class);

    /**
     * Simple broadcastreceiver don't forget to register it in the manifest file
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        LOGD(TAG, "Recurring alarm; requesting download service.");

        final Intent downloader = new Intent(context, TutListDownloaderService.class);
        downloader.setData(Uri.parse("http://feeds.feedburner.com/MobileTuts?format=xml"));
        context.startService(downloader);
    }
}
