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
import static com.olheingenieros.listexample.utils.LogUtils.LOGE;
import static com.olheingenieros.listexample.utils.LogUtils.LOGW;
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.olheingenieros.listexample.TutListActivity;
import com.olheingenieros.listexample.provider.TutListDatabase;
import com.olheingenieros.listexample.provider.TutListProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service for downloading info from the db in background
 * 
 * @author olheingenierossl
 * @since 1.0
 */
public class TutListDownloaderService extends Service {

    private static final String TAG = makeLogTag(TutListDownloaderService.class);
    private DownloaderTask tutorialDownloader;

    private static final int LIST_UPDATE_NOTIFICATION = 100;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startID) {
        URL tutorialPath;
        try {
            tutorialPath = new URL(intent.getDataString());
            tutorialDownloader = new DownloaderTask();
            tutorialDownloader.execute(tutorialPath);
        } catch (final MalformedURLException e) {
            LOGE(TAG, "BAD URL", e);
        }
        return Service.START_FLAG_REDELIVERY;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private class DownloaderTask extends AsyncTask<URL, Void, Boolean> {
        private final String TAG = makeLogTag(DownloaderTask.class);

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground(final URL... params) {
            boolean succeeded = false;
            LOGD(TAG, "Background Service downloading xml");
            final URL downloadPath = params[0];

            if (downloadPath != null) {
                succeeded = xmlParse(downloadPath);
            }
            return succeeded;

        }

        private boolean xmlParse (final URL downloadPath) {
            boolean succeeded = false;
            LOGD(TAG, "Parsing XML");

            XmlPullParser datos;

            try {
                datos = XmlPullParserFactory.newInstance().newPullParser();
                datos.setInput(downloadPath.openStream(), null);
                int eventType = -1;

                /*
                 * for each item, find link and title
                 */
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (datos.getName().equals("item")) {

                            final ContentValues tutorialData = new ContentValues();

                            //Inner loop looking for link and title
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    if (datos.getName().equals("link")) {
                                        datos.next();
                                        LOGD(TAG, "Link: " + datos.getText());
                                        tutorialData.put(TutListDatabase.COL_URL, datos.getText());
                                    } else if (datos.getName().equals("title")) {
                                        datos.next();
                                        tutorialData.put(TutListDatabase.COL_TITLE, datos.getText());
                                    } else if (datos.getName().equals("pubDate")){
                                        // Get the Date
                                        datos.next();

                                        /*
                                         * We expect date in format
                                         * <pubDate>Fri, 20 May 2011 11:30:23
                                         * +0000</pubDate>
                                         */
                                        final DateFormat parser = new SimpleDateFormat("E, dd MMM yyyy");
                                        try {
                                            final Date date = parser.parse(datos.getText());
                                            tutorialData.put(TutListDatabase.COL_DATE,
                                                    date.getTime() / 1000);
                                        } catch (final java.text.ParseException e) {
                                            LOGE(TAG, "Error parsing date: " + datos.getText());
                                        }
                                    }
                                } else if (eventType == XmlPullParser.END_TAG) {
                                    if (datos.getName().equals("item")) {

                                        // Save data and continue with outer loop
                                        getContentResolver().insert(TutListProvider.CONTENT_URI, tutorialData);
                                        break;
                                    }

                                }
                                eventType = datos.next();
                            }
                        }
                    }
                    eventType = datos.next();
                }
                succeeded = true;
            } catch (final XmlPullParserException e) {
                LOGE(TAG, "Error during parsing", e);
                e.printStackTrace();
            } catch (final IOException e) {
                LOGE(TAG, "IO ERROR during parsing", e);
                e.printStackTrace();
            }

            return succeeded;
        }

        @Override
        protected void onPostExecute(final Boolean result) {

            /*
             * Code for notifications, using notification compat builder
             * @see
             * http://stackoverflow.com/questions/6391870/how-exactly-to-use
             * -notification-builder
             */
            final Context context = getApplicationContext();
            final NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(NOTIFICATION_SERVICE);

            // Create PendingIntent
            final Intent notificationIntent = new Intent(context, TutListActivity.class);
            final PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);

            final Resources res = context.getResources();

            // We create the notification using the compat library
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            String contentText;

            if (!result) {
                LOGW(TAG, "XML download and parse had errors");
                contentText = context.getText(
                        com.olheingenieros.listexample.R.string.notification_info_fail).toString();
            } else {
                contentText = context.getText(
                        com.olheingenieros.listexample.R.string.notification_info_success)
                        .toString();
            }

            builder.setContentIntent(contentIntent)
            .setSmallIcon(com.olheingenieros.listexample.R.drawable.ic_stat_sync)
            .setLargeIcon(
                    BitmapFactory.decodeResource(res,
                            com.olheingenieros.listexample.R.drawable.ic_stat_sync))
                            .setTicker(
                                    res.getString(com.olheingenieros.listexample.R.string.notification_title))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(
                                            res.getString(com.olheingenieros.listexample.R.string.notification_title))
                                            .setContentText(contentText);

            // Create the notification
            final Notification updateComplete = builder.getNotification();

            // Notify the user
            notificationManager.notify(LIST_UPDATE_NOTIFICATION, updateComplete);
        }
    }

}
