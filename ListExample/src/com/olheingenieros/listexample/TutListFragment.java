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
import static com.olheingenieros.listexample.utils.LogUtils.makeLogTag;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
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
import com.olheingenieros.listexample.provider.TutListSharedPrefs;
import com.olheingenieros.listexample.sync.TutListDownloaderService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TutListFragment extends SherlockListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {
    private OnTutSelectedListener tutSelectedListener;
    private static final int TUTORIAL_LIST_LOADER = 0x01;
    private SimpleCursorAdapter adapter;

    private static final String TAG = makeLogTag(TutListFragment.class);

    // For readed items
    private static final String LAST_POSITION_KEY = "lastPosition";
    private static final String LAST_ITEM_CLICKED_KEY = "lastItemClicked";
    private static final String CUR_TUT_URL_KEY = "curTutUrl";

    private long lastItemClicked = -1;
    private String curTutUrl = null;
    private int selectedPosition = -1;

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        if (position == selectedPosition) {
            // Same selection, skip
            return;
        }

        final String projection[] = {
                TutListDatabase.COL_URL
        };

        // GET viewed data uri
        final Uri viewedTut = Uri.withAppendedPath(TutListProvider.CONTENT_URI, String.valueOf(id));

        final Cursor c = getSherlockActivity().getContentResolver().query(
                viewedTut, projection,
                null, null, null);
        if (c.moveToFirst()) {
            final String dataUrl = c.getString(0);
            tutSelectedListener.onTutSelected(dataUrl);
        }
        c.close();
        LOGI(TAG, "MARKING ITEM AS READ");

        // Mark data as read
        if (lastItemClicked != -1) {
            TutListProvider.markItemRead(getSherlockActivity().getApplicationContext(),
                    lastItemClicked);
            LOGI(TAG, "Marking " + lastItemClicked + " as read. Now Showing " + id + ".");
        }
        lastItemClicked = id;

        // v11+ highlights
        selectedPosition = position;
        l.setItemChecked(position, true);
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
        LOGI(TAG, "On create Fragment");
    }

    // Fragment Life Cycle
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create and init cursor loader
        getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.list_item,
                null, UI_BINDING_FROM, UI_BINDING_TO,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapter.setViewBinder(new TutorialViewBinder());
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        setEmptyText(getResources().getText(R.string.empty_list_label));
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (savedInstanceState != null) {
            lastItemClicked = savedInstanceState.getLong(LAST_ITEM_CLICKED_KEY, -1);
            selectedPosition = savedInstanceState.getInt(LAST_POSITION_KEY, -1);
            if (selectedPosition != -1) {
                setSelection(selectedPosition);
                getListView().smoothScrollToPosition(selectedPosition);
                getListView().setItemChecked(selectedPosition, true);
            }

            curTutUrl = savedInstanceState.getString(CUR_TUT_URL_KEY);
            if (curTutUrl != null) {
                tutSelectedListener.onTutSelected(curTutUrl);
            }

        }
    }

    /* @var boolean */
    private boolean showReadFlag;

    @Override
    public void onPause() {
        showReadFlag = TutListSharedPrefs.getOnlyUnreadFlag(getSherlockActivity());
        LOGI(TAG, "ON PAUSE");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LOGI(TAG, "ON RESUME");
        if (showReadFlag != TutListSharedPrefs.getOnlyUnreadFlag(getSherlockActivity())) {
            getLoaderManager().restartLoader(TUTORIAL_LIST_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(LAST_ITEM_CLICKED_KEY, lastItemClicked);
        outState.putString(CUR_TUT_URL_KEY, curTutUrl);
        outState.putInt(LAST_POSITION_KEY, selectedPosition);
    }

    @Override
    public void onDestroy() {
        LOGI(TAG, "ON DESTROY");
        super.onDestroy();
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

        // pref menu item
        final Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
                TutListPreferencesActivity.class);
        final MenuItem preferences = menu.findItem(R.id.settings_option_item);
        preferences.setIntent(prefsIntent);
    }

    /**
     * Catch every item menu clicked and dispatch its action
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_option_item:
                getActivity().startService(item.getIntent());
                break;
            case R.id.settings_option_item:
                getActivity().startActivity(item.getIntent());
                break;
            case R.id.mark_all_read_item:
                TutListProvider.markAllItemsRead(getActivity().getApplicationContext());
                break;
        }
        return true;
    }

    // custom viewbinder
    private class TutorialViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(final View view, final Cursor cursor, final int index) {
            if (index == cursor.getColumnIndex(TutListDatabase.COL_DATE)) {
                // get a locale based string for the date
                // final DateFormat formatter = android.text.format.DateFormat
                // .getDateFormat(getSherlockActivity().getApplicationContext());
                final DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyy",
                        Locale.getDefault());
                final long date = cursor.getLong(index);
                final Date dateObj = new Date(date * 1000);
                ((TextView) view).setText(formatter.format(dateObj));

                return true;
            } else if (index == cursor.getColumnIndex(TutListDatabase.COL_READ)) {
                final boolean read = cursor.getInt(index) > 0 ? true : false;
                final TextView title = (TextView) view;
                if (!read) {
                    title.setTypeface(Typeface.DEFAULT_BOLD, 0);

                } else {
                    title.setTypeface(Typeface.DEFAULT);
                }
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
                TutListDatabase.ID, TutListDatabase.COL_TITLE, TutListDatabase.COL_DATE,
                TutListDatabase.COL_READ
        };

        final Uri content = TutListProvider.CONTENT_URI;

        // For preferences
        String selection = null;
        if (TutListSharedPrefs.getOnlyUnreadFlag(getSherlockActivity())) {
            selection = TutListDatabase.COL_READ + " ='0'";
        }

        /*
         * Last column is the order column, we can change that when defining the
         * provider
         */
        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
                content, projection, selection, null, TutListDatabase.COL_DATE + " desc");
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
