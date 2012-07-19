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

import static com.olheingenieros.listexample.utils.LogUtils.LOGW;
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Class who handles the Database basic operations
 * 
 * @author olheingenierossl
 * @since 1.0
 */
public class TutListDatabase extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(TutListDatabase.class);

    // DB DATA
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "list_data";

    // TABLE DATA
    public static final String TABLE_DATOS = "datos";
    public static final String ID = "_id";
    public static final String COL_TITLE = "datos_title";
    public static final String COL_URL = "datos_url";

    // TABLE CREATE SQL
    private static final String CREATE_TABLE_DATOS = "CREATE TABLE " + TABLE_DATOS
            + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT NOT NULL, "
            + COL_URL + " TEXT NOT NULL);";
    private static final String DB_SCHEMA = CREATE_TABLE_DATOS;

    /**
     * Constructor for class {@link #TutListDatabase}
     * 
     * @param context
     */
    public TutListDatabase(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA);

    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        LOGW(TAG, "Upgrading Database. Existing contents will be lost. ["
                + oldVersion + "]->[" + newVersion + "]");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATOS);
        onCreate(db);

    }


}
