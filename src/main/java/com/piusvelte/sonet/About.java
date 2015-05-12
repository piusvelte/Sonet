/*
 * Sonet - Android Social Networking Widget
 * Copyright (C) 2009 Bryan Emmanuel
 * 
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Bryan Emmanuel piusvelte@gmail.com
 */
package com.piusvelte.sonet;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.piusvelte.sonet.fragment.ConfirmationDialogFragment;
import com.piusvelte.sonet.fragment.ItemsDialogFragment;
import com.piusvelte.sonet.fragment.WidgetsList;
import com.piusvelte.sonet.provider.Accounts;
import com.piusvelte.sonet.provider.Widgets;
import com.piusvelte.sonet.provider.WidgetsSettings;

import static com.piusvelte.sonet.Sonet.ACTION_REFRESH;
import static com.piusvelte.sonet.Sonet.PRO;
import static com.piusvelte.sonet.Sonet.RESULT_REFRESH;

public class About extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private int[] mAppWidgetIds;
    private AppWidgetManager mAppWidgetManager;
    private boolean mUpdateWidget = false;
    private static final String TAG = "About";

    private static final String FRAGMENT_WIDGETS_LIST = "widgets_list";

    private static final int LOADER_DEFAULT_WIDGET = 0;

    private static final String DIALOG_ABOUT = "dialog:about";
    private static final String DIALOG_WIDGETS = "dialog:widgets";

    private static final int REQUEST_WIDGET = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        if (!getPackageName().toLowerCase().contains(PRO)) {
            AdView adView = new AdView(this, AdSize.BANNER, BuildConfig.GOOGLEAD_ID);
            ((FrameLayout) findViewById(R.id.ad)).addView(adView);
            adView.loadAd(new AdRequest());
        }

        Fragment widgetsList = getSupportFragmentManager().findFragmentByTag(FRAGMENT_WIDGETS_LIST);

        if (widgetsList == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.widgets_list_container, new WidgetsList(), FRAGMENT_WIDGETS_LIST)
                    .commit();
        }

        getSupportLoaderManager().initLoader(LOADER_DEFAULT_WIDGET, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_about_refresh) {
            startService(new Intent(this, SonetService.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID).setAction(ACTION_REFRESH));
            return true;
        } else if (itemId == R.id.menu_about_accounts_and_settings) {
            startActivity(new Intent(this, ManageAccounts.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
            return true;
        } else if (itemId == R.id.menu_about_default_settings) {
            startActivityForResult(new Intent(this, Settings.class), RESULT_REFRESH);
            return true;
        } else if (itemId == R.id.menu_about_refresh_widgets) {
            startService(new Intent(this, SonetService.class).setAction(ACTION_REFRESH)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetIds));
            return true;
        } else if (itemId == R.id.menu_about_notifications) {
            startActivity(new Intent(this, SonetNotifications.class));
            return true;
        } else if (itemId == R.id.menu_about_widget_settings) {
            if (mAppWidgetIds.length > 0) {
                String[] widgets = new String[mAppWidgetIds.length];

                for (int i = 0, i2 = mAppWidgetIds.length; i < i2; i++) {
                    AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(mAppWidgetIds[i]);
                    String providerName = info.provider.getClassName();
                    widgets[i] = Integer.toString(mAppWidgetIds[i]) + " (" + providerName + ")";
                }

                ItemsDialogFragment.newInstance(widgets, REQUEST_WIDGET)
                        .show(getSupportFragmentManager(), DIALOG_WIDGETS);
            } else {
                Toast.makeText(this, getString(R.string.nowidgets), Toast.LENGTH_LONG).show();
            }

            return true;
        } else if (itemId == R.id.menu_about_about) {
            ConfirmationDialogFragment.newInstance(R.string.about_title, R.string.about, -1)
                    .show(getSupportFragmentManager(), DIALOG_ABOUT);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppWidgetIds = new int[0];
        // validate appwidgetids from appwidgetmanager
        mAppWidgetManager = AppWidgetManager.getInstance(About.this);
        mAppWidgetIds = Sonet.getWidgets(getApplicationContext(), mAppWidgetManager);
    }

    @Override
    protected void onPause() {
        if (mUpdateWidget) {
            (Toast.makeText(getApplicationContext(), getString(R.string.refreshing), Toast.LENGTH_LONG)).show();
            startService(new Intent(this, SonetService.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetIds));
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REFRESH:
                if (resultCode == RESULT_OK) {
                    mUpdateWidget = true;
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case REQUEST_WIDGET:
                startActivity(new Intent(this, ManageAccounts.class)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds[ItemsDialogFragment.getWhich(data, 0)]));
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_DEFAULT_WIDGET:
                return new CursorLoader(this, WidgetsSettings.getContentUri(this),
                        new String[] { Widgets.DISPLAY_PROFILE },
                        Widgets.WIDGET + "=? and " + Widgets.ACCOUNT + "=?",
                        new String[] { String.valueOf(AppWidgetManager.INVALID_APPWIDGET_ID), String.valueOf(Accounts.INVALID_ACCOUNT_ID) },
                        null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_DEFAULT_WIDGET:
                if (cursor != null && cursor.moveToFirst()) {
                    // TODO used to determine showing profile in WidgetsList
                    //boolean showProfile = cursor.getInt(cursor.getColumnIndex(Widgets.DISPLAY_PROFILE)) != 0;
                } else {
                    // initialize account settings
                    ContentValues values = new ContentValues();
                    values.put(Widgets.WIDGET, AppWidgetManager.INVALID_APPWIDGET_ID);
                    values.put(Widgets.ACCOUNT, Sonet.INVALID_ACCOUNT_ID);
                    getContentResolver().insert(Widgets.getContentUri(About.this), values).getLastPathSegment();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // NO-OP
    }
}
