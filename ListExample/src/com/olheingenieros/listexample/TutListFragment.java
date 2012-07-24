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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.olheingenieros.listexample.provider.TutListDatabase;
import com.olheingenieros.listexample.provider.TutListProvider;
import com.olheingenieros.listexample.sync.TutListDownloaderService;

import java.text.DateFormat;
import java.util.Date;

public class TutListFragment extends SherlockListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {
    private OnTutSelectedListener tutSelectedListener;
    private static final int TUTORIAL_LIST_LOADER = 0x01;
    private SimpleCursorAdapter adapter;

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final String projection[] = {
                TutListDatabase.COL_URL
        };
        final Cursor c = getActivity().getContentResolver().query(
                Uri.withAppendedPath(TutListProvider.CONTENT_URI, String.valueOf(id)), projection,
                null, null, null);
        if (c.moveToFirst()) {
            final String dataUrl = c.getString(0);
            tutSelectedListener.onTutSelected(dataUrl);
        }
        c.close();
    }

    private static final String[] UI_BINDING_FROM = {
        TutListDatabase.COL_TITLE, TutListDatabase.COL_DATE
    };
    private static final int[] UI_BINDING_TO = {
        R.id.title, R.id.date
    };
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Create and init cursor loader
        getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.list_item,
                null, UI_BINDING_FROM, UI_BINDING_TO,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapter.setViewBinder(new TutorialViewBinder());
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    public interface OnTutSelectedListener {
        public void onTutSelected(String tutUrl);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            tutSelectedListener = (OnTutSelectedListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTutSelectedListener");
        }
    }

    // options menu

    private int refreshMenuId;

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        final Intent intent = new Intent(getActivity().getApplicationContext(),
                TutListDownloaderService.class);
        intent.setData(Uri
                .parse("http://feeds.feedburner.com/MobileTuts?format=xml"));
        inflater.inflate(R.menu.options_menu, menu);
        final MenuItem refresh = menu.findItem(R.id.refresh_option_item);
        refresh.setIntent(intent);
        refreshMenuId = refresh.getItemId();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == refreshMenuId) {
            getActivity().startService(item.getIntent());
        }
        return true;
    }

    // custom viewbinder
    private class TutorialViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(final View view, final Cursor cursor, final int index) {
            if (index == cursor.getColumnIndex(TutListDatabase.COL_DATE)) {
                // get a locale based string for the date
                final DateFormat formatter = android.text.format.DateFormat
                        .getDateFormat(getActivity().getApplicationContext());
                final long date = cursor.getLong(index);
                final Date dateObj = new Date(date * 1000);
                ((TextView) view).setText(formatter.format(dateObj));
                return true;
            } else {
                return false;
            }
        }
    }

    // LoaderManager.LoaderCallBacks<Cursor> methods

    /*
     * (non-Javadoc)
     * @see
     * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        // Each of the columns used in the DB that we want to show
        final String[] projection = {
                TutListDatabase.ID, TutListDatabase.COL_TITLE, TutListDatabase.COL_DATE
        };

        final Uri content = TutListProvider.CONTENT_URI;

        /*
         * Last column is the order column, we can change that when defining the
         * provider
         */
        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
                content, projection, null, null, TutListDatabase.COL_DATE + " desc");
        return cursorLoader;
    }

    /**
     * Assigns the new cursor but doesn't close the previous one so the system
     * optimizes it where appropiate
     * 
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     *      .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    /**
     * This method is triggered when the loader is being reset and the loader
     * data is not avaible.
     * 
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     *      .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        // Clear the cursor we were using
        adapter.swapCursor(null);

    }
}
