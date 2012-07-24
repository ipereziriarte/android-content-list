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
package com.olheingenieros.listexample.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.olheingenieros.listexample.TutListActivity;
import com.olheingenieros.listexample.provider.TutListDatabase;

import java.text.DateFormat;
import java.util.Date;

/**
 * Class that implements a custom view binder to show the db data in our view
 * 
 * @author olheingenierossl
 * @since 3.0
 */
public class TutorialViewBinder implements SimpleCursorAdapter.ViewBinder {

    /*
     * (non-Javadoc)
     * @see
     * android.support.v4.widget.SimpleCursorAdapter.ViewBinder#setViewValue
     * (android.view.View, android.database.Cursor, int)
     */
    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int index) {
        if (index == cursor.getColumnIndex(TutListDatabase.COL_DATE)) {
            // Get a locale based string for the date
            final Activity a = new TutListActivity();
            final Context context = a.getApplicationContext();
            final DateFormat formatter = android.text.format.DateFormat.getDateFormat(context);
            final long date = cursor.getLong(index);
            final Date dateObj = new Date(date * 1000);
            ((TextView) view).setText(formatter.format(dateObj));
            return true;
        }
        return false;
    }

}
