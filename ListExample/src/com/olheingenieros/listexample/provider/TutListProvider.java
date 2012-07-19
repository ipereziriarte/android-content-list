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
package com.olheingenieros.listexample.provider;

import static com.olheingenieros.listexample.utils.LogUtils.LOGI;
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


/**
 * Content Provider Class
 * 
 * @author olheingenierossl
 */
public class TutListProvider extends ContentProvider {
    private TutListDatabase mDB;
    private static final String TAG = makeLogTag(TutListProvider.class);

    // URIs to identify the data we want to query
    private static final String AUTHORITY = "com.olheingenieros.listexample.provider.TutListProvider";
    public static final int DATOS = 100;
    public static final int DATO_ID = 101;

    private static final String DATOS_BASE_PATH = "datos";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + DATOS_BASE_PATH);
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "vnd.listexample.dato";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "vnd.listexample.dato";

    /*
     * Matcher for pairing data the first one gets all data, the second one only
     * the one with the id provided after the slash
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, DATOS_BASE_PATH, DATOS);
        sUriMatcher.addURI(AUTHORITY, DATOS_BASE_PATH + "/#", DATO_ID);
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final int uriType = sUriMatcher.match(uri);
        final SQLiteDatabase sqlDb = mDB.getWritableDatabase();
        int rowsAffected = 0;
        switch (uriType) {
            case DATOS :
                rowsAffected = sqlDb.delete(TutListDatabase.TABLE_DATOS, selection, selectionArgs);
                break;
            case DATO_ID:
                final String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDb.delete(TutListDatabase.TABLE_DATOS, TutListDatabase.ID + "=" + id, null);
                } else {
                    rowsAffected = sqlDb.delete(TutListDatabase.TABLE_DATOS, selection + " and, " + TutListDatabase.ID + "=" +id, selectionArgs);

                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(final Uri uri) {
        final int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case DATOS:
                return CONTENT_TYPE;
            case DATO_ID:
                return CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri,
     * android.content.ContentValues)
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final int uriType = sUriMatcher.match(uri);
        if (uriType != DATOS) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }
        final SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        try {
            final long newID = sqlDB.insertOrThrow(TutListDatabase.TABLE_DATOS, null, values);
            if (newID > 0) {
                final Uri newUri = ContentUris.withAppendedId(uri, newID);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }

        } catch (final SQLiteConstraintException e) {
            LOGI(TAG, "Ignoring constraint failure.");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        mDB = new TutListDatabase(getContext());
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri,
     * java.lang.String[], java.lang.String, java.lang.String[],
     * java.lang.String)
     */
    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs,
            final String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TutListDatabase.TABLE_DATOS);

        final int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case DATO_ID:
                queryBuilder.appendWhere(TutListDatabase.ID + "=" + uri.getLastPathSegment());
                break;
            case DATOS:
                // No filter
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        final Cursor c = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri,
     * android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(final Uri uri, final ContentValues values, final String selection,
            final String[] selectionArgs) {

        final SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        int rowsAffected;

        final int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case DATO_ID:
                final String id = uri.getLastPathSegment();
                final StringBuilder modSelection = new StringBuilder(TutListDatabase.ID + "=" + id);
                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND " + selection);
                }

                rowsAffected = sqlDB.update(TutListDatabase.TABLE_DATOS, values,
                        modSelection.toString(), null);
                break;
            case DATOS:
                rowsAffected = sqlDB.update(TutListDatabase.TABLE_DATOS, values, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;

    }

}
