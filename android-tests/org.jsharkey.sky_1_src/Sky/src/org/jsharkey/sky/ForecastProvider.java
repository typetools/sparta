/*
 * Copyright (C) 2009 Jeff Sharkey, http://jsharkey.org/
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

package org.jsharkey.sky;

import android.appwidget.AppWidgetManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Provider that holds widget configuration details, and any cached forecast
 * data. Provides easy {@link ContentResolver} access to the data when building
 * widget updates or showing detailed lists.
 */
public class ForecastProvider extends ContentProvider {
    private static final String TAG = "ForecastProvider";
    private static final boolean LOGD = true;

    public static final String AUTHORITY = "org.jsharkey.sky";

    public interface AppWidgetsColumns {
        /**
         * Title given by user to this widget, usually shown in medium widgets
         * and details title bar.
         */
        public static final String TITLE = "title";
        public static final String LAT = "lat";
        public static final String LON = "lon";

        /**
         * Temperature units to use when displaying forecasts for this widget,
         * usually defaults to {@link #UNITS_FAHRENHEIT}.
         */
        public static final String UNITS = "units";
        public static final int UNITS_FAHRENHEIT = 1;
        public static final int UNITS_CELSIUS = 2;

        /**
         * Last system time when forecasts for this widget were updated, usually
         * as read from {@link System#currentTimeMillis()}.
         */
        public static final String LAST_UPDATED = "lastUpdated";

        /**
         * Flag specifying if this widget has been configured yet, used to skip
         * building widget updates.
         */
        public static final String CONFIGURED = "configured";

        public static final int CONFIGURED_TRUE = 1;
    }

    public static class AppWidgets implements BaseColumns, AppWidgetsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/appwidgets");

        /**
         * Directory twig to request all forecasts for a specific widget.
         */
        public static final String TWIG_FORECASTS = "forecasts";

        /**
         * Directory twig to request the forecast nearest the requested time.
         */
        public static final String TWIG_FORECAST_AT = "forecast_at";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/appwidget";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/appwidget";

    }

    public interface ForecastsColumns {
        /**
         * The parent {@link AppWidgetManager#EXTRA_APPWIDGET_ID} of this
         * forecast.
         */
        public static final String APPWIDGET_ID = "widgetId";

        /**
         * Flag if this forecast is an alert.
         */
        public static final String ALERT = "alert";
        public static final int ALERT_TRUE = 1;

        /**
         * Timestamp when this forecast becomes valid, in base ready for
         * comparison with {@link System#currentTimeMillis()}.
         */
        public static final String VALID_START = "validStart";

        /**
         * High temperature during this forecast period, stored in Fahrenheit.
         */
        public static final String TEMP_HIGH = "tempHigh";

        /**
         * Low temperature during this forecast period, stored in Fahrenheit.
         */
        public static final String TEMP_LOW = "tempLow";

        /**
         * String describing the weather conditions.
         */
        public static final String CONDITIONS = "conditions";

        /**
         * Web link where more details can be found about this forecast.
         */
        public static final String URL = "url";

    }

    public static class Forecasts implements BaseColumns, ForecastsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forecasts");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/forecast";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/forecast";
        
    }

    private static final String TABLE_APPWIDGETS = "appwidgets";
    private static final String TABLE_FORECASTS = "forecasts";

    private DatabaseHelper mOpenHelper;

    /**
     * Helper to manage upgrading between versions of the forecast database.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "forecasts.db";

        private static final int DATABASE_VERSION = 2;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_APPWIDGETS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + AppWidgetsColumns.TITLE + " TEXT,"
                    + AppWidgetsColumns.LAT + " REAL,"
                    + AppWidgetsColumns.LON + " REAL,"
                    + AppWidgetsColumns.UNITS + " INTEGER,"
                    + AppWidgetsColumns.LAST_UPDATED + " INTEGER,"
                    + AppWidgetsColumns.CONFIGURED + " INTEGER);");

            db.execSQL("CREATE TABLE " + TABLE_FORECASTS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ForecastsColumns.APPWIDGET_ID + " INTEGER,"
                    + ForecastsColumns.ALERT + " INTEGER DEFAULT 0,"
                    + ForecastsColumns.VALID_START + " INTEGER,"
                    + ForecastsColumns.TEMP_HIGH + " INTEGER,"
                    + ForecastsColumns.TEMP_LOW + " INTEGER,"
                    + ForecastsColumns.CONDITIONS + " TEXT,"
                    + ForecastsColumns.URL + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying old data during upgrade.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPWIDGETS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORECASTS);
                onCreate(db);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (LOGD) Log.d(TAG, "delete() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;

        switch (sUriMatcher.match(uri)) {
            case APPWIDGETS: {
                count = db.delete(TABLE_APPWIDGETS, selection, selectionArgs);
                break;
            }
            case APPWIDGETS_ID: {
                // Delete a specific widget and all its forecasts
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                count = db.delete(TABLE_APPWIDGETS, BaseColumns._ID + "=" + appWidgetId, null);
                count += db.delete(TABLE_FORECASTS, ForecastsColumns.APPWIDGET_ID + "="
                        + appWidgetId, null);
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Delete all the forecasts for a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                if (selection == null) {
                    selection = "";
                } else {
                    selection = "(" + selection + ") AND ";
                }
                selection += ForecastsColumns.APPWIDGET_ID + "=" + appWidgetId;
                count = db.delete(TABLE_FORECASTS, selection, selectionArgs);
                break;
            }
            case FORECASTS: {
                count = db.delete(TABLE_FORECASTS, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case APPWIDGETS:
                return AppWidgets.CONTENT_TYPE;
            case APPWIDGETS_ID:
                return AppWidgets.CONTENT_ITEM_TYPE;
            case APPWIDGETS_FORECASTS:
                return Forecasts.CONTENT_TYPE;
            case FORECASTS:
                return Forecasts.CONTENT_TYPE;
            case FORECASTS_ID:
                return Forecasts.CONTENT_ITEM_TYPE;
        }
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (LOGD) Log.d(TAG, "insert() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri resultUri = null;

        switch (sUriMatcher.match(uri)) {
            case APPWIDGETS: {
                long rowId = db.insert(TABLE_APPWIDGETS, AppWidgetsColumns.TITLE, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(AppWidgets.CONTENT_URI, rowId);
                }
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Insert a forecast into a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                values.put(ForecastsColumns.APPWIDGET_ID, appWidgetId);
                long rowId = db.insert(TABLE_FORECASTS, ForecastsColumns.CONDITIONS, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(AppWidgets.CONTENT_URI, rowId);
                }
                break;
            }
            case FORECASTS: {
                long rowId = db.insert(TABLE_FORECASTS, ForecastsColumns.CONDITIONS, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(Forecasts.CONTENT_URI, rowId);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return resultUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        if (LOGD) Log.d(TAG, "query() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String limit = null;

        switch (sUriMatcher.match(uri)) {
            case APPWIDGETS: {
                qb.setTables(TABLE_APPWIDGETS);
                break;
            }
            case APPWIDGETS_ID: {
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_APPWIDGETS);
                qb.appendWhere(BaseColumns._ID + "=" + appWidgetId);
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Pick all the forecasts for given widget, sorted by date and
                // importance
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_FORECASTS);
                qb.appendWhere(ForecastsColumns.APPWIDGET_ID + "=" + appWidgetId);
                sortOrder = ForecastsColumns.VALID_START + " ASC, " + ForecastsColumns.ALERT
                        + " DESC";
                break;
            }
            case APPWIDGETS_FORECAST_AT: {
                // Pick the forecast nearest for given widget nearest the given
                // timestamp
                String appWidgetId = uri.getPathSegments().get(1);
                String atTime = uri.getPathSegments().get(3);
                qb.setTables(TABLE_FORECASTS);
                qb.appendWhere(ForecastsColumns.APPWIDGET_ID + "=" + appWidgetId);
                sortOrder = "ABS(" + ForecastsColumns.VALID_START + " - " + atTime + ") ASC, "
                        + ForecastsColumns.ALERT + " DESC";
                limit = "1";
                break;
            }
            case FORECASTS: {
                qb.setTables(TABLE_FORECASTS);
                break;
            }
            case FORECASTS_ID: {
                String forecastId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_FORECASTS);
                qb.appendWhere(BaseColumns._ID + "=" + forecastId);
                break;
            }
        }

        return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (LOGD) Log.d(TAG, "update() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case APPWIDGETS: {
                return db.update(TABLE_APPWIDGETS, values, selection, selectionArgs);
            }
            case APPWIDGETS_ID: {
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                return db.update(TABLE_APPWIDGETS, values, BaseColumns._ID + "=" + appWidgetId,
                        null);
            }
            case FORECASTS: {
                return db.update(TABLE_FORECASTS, values, selection, selectionArgs);
            }
        }

        throw new UnsupportedOperationException();
    }

    /**
     * Matcher used to filter an incoming {@link Uri}. 
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int APPWIDGETS = 101;
    private static final int APPWIDGETS_ID = 102;
    private static final int APPWIDGETS_FORECASTS = 103;
    private static final int APPWIDGETS_FORECAST_AT = 104;

    private static final int FORECASTS = 201;
    private static final int FORECASTS_ID = 202;

    static {
        sUriMatcher.addURI(AUTHORITY, "appwidgets", APPWIDGETS);
        sUriMatcher.addURI(AUTHORITY, "appwidgets/#", APPWIDGETS_ID);
        sUriMatcher.addURI(AUTHORITY, "appwidgets/#/forecasts", APPWIDGETS_FORECASTS);
        sUriMatcher.addURI(AUTHORITY, "appwidgets/#/forecast_at/*", APPWIDGETS_FORECAST_AT);

        sUriMatcher.addURI(AUTHORITY, "forecasts", FORECASTS);
        sUriMatcher.addURI(AUTHORITY, "forecasts/#", FORECASTS_ID);
    }
}
