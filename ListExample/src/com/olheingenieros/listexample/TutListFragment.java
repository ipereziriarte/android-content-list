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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.olheingenieros.listexample.provider.TutListDatabase;
import com.olheingenieros.listexample.provider.TutListProvider;

public class TutListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
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

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String[] uiBindFrom = {
                TutListDatabase.COL_TITLE
        };
        final int[] uiBindTo = {
                R.id.title
        };

        // Create and init cursor loader
        getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.list_item,
                null, uiBindFrom, uiBindTo,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setListAdapter(adapter);
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

    /*
     * (non-Javadoc)
     * @see
     * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String[] projection = {
                TutListDatabase.ID, TutListDatabase.COL_TITLE
        };

        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
                TutListProvider.CONTENT_URI, projection, null, null, null);
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
