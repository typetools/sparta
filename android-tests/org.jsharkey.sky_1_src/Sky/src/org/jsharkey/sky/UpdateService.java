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

import java.util.LinkedList;
import java.util.Queue;

import org.jsharkey.sky.ForecastProvider.AppWidgets;
import org.jsharkey.sky.ForecastProvider.AppWidgetsColumns;
import org.jsharkey.sky.WebserviceHelper.ForecastParseException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Background service to build any requested widget updates. Uses a single
 * background thread to walk through an update queue, querying
 * {@link WebserviceHelper} as needed to fill database. Also handles scheduling
 * of future updates, usually in 6-hour increments.
 */
public class UpdateService extends Service implements Runnable {
    private static final String TAG = "UpdateService";

    private static final String[] PROJECTION_APPWIDGETS = new String[] {
        AppWidgetsColumns.CONFIGURED,
        AppWidgetsColumns.LAST_UPDATED,
    };

    private static final int COL_CONFIGURED = 0;
    private static final int COL_LAST_UPDATED = 1;

    /**
     * Interval to wait between background widget updates. Every 6 hours is
     * plenty to keep background data usage low and still provide fresh data.
     */
    private static final long UPDATE_INTERVAL = 6 * DateUtils.HOUR_IN_MILLIS;

    /**
     * When rounding updates to the nearest-top-of-hour, trigger the update
     * slightly early by this amount. This makes sure that we're already updated
     * when the user's 6AM alarm clock goes off.
     */
    private static final long UPDATE_TRIGGER_EARLY = 10 * DateUtils.MINUTE_IN_MILLIS;

    /**
     * If we calculated an update too quickly in the future, wait this interval
     * and try rescheduling.
     */
    private static final long UPDATE_THROTTLE = 30 * DateUtils.MINUTE_IN_MILLIS;

    /**
     * Specific {@link Intent#setAction(String)} used when performing a full
     * update of all widgets, usually when an update alarm goes off.
     */
    public static final String ACTION_UPDATE_ALL = "org.jsharkey.sky.UPDATE_ALL";

    /**
     * Length of time before we consider cached forecasts stale. If a widget
     * update is requested, and {@link AppWidgetsColumns#LAST_UPDATED} is inside
     * this threshold, we use the cached forecast data to build the update.
     * Otherwise, we first trigger an update through {@link WebserviceHelper}.
     */
    private static final long FORECAST_CACHE_THROTTLE = 3 * DateUtils.HOUR_IN_MILLIS;

    /**
     * Number of days into the future to request forecasts for.
     */
    private static final int FORECAST_DAYS = 4;

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private static Object sLock = new Object();

    /**
     * Flag if there is an update thread already running. We only launch a new
     * thread if one isn't already running.
     */
    private static boolean sThreadRunning = false;

    /**
     * Internal queue of requested widget updates. You <b>must</b> access
     * through {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make
     * sure your access is correctly synchronized.
     */
    private static Queue<Integer> sAppWidgetIds = new LinkedList<Integer>();

    /**
     * Request updates for the given widgets. Will only queue them up, you are
     * still responsible for starting a processing thread if needed, usually by
     * starting the parent service.
     */
    public static void requestUpdate(int[] appWidgetIds) {
        synchronized (sLock) {
            for (int appWidgetId : appWidgetIds) {
                sAppWidgetIds.add(appWidgetId);
            }
        }
    }

    /**
     * Peek if we have more updates to perform. This method is special because
     * it assumes you're calling from the update thread, and that you will
     * terminate if no updates remain. (It atomically resets
     * {@link #sThreadRunning} when none remain to prevent race conditions.)
     */
    private static boolean hasMoreUpdates() {
        synchronized (sLock) {
            boolean hasMore = !sAppWidgetIds.isEmpty();
            if (!hasMore) {
                sThreadRunning = false;
            }
            return hasMore;
        }
    }

    /**
     * Poll the next widget update in the queue.
     */
    private static int getNextUpdate() {
        synchronized (sLock) {
            if (sAppWidgetIds.peek() == null) {
                return AppWidgetManager.INVALID_APPWIDGET_ID;
            } else {
                return sAppWidgetIds.poll();
            }
        }
    }

    /**
     * Start this service, creating a background processing thread, if not
     * already running. If started with {@link #ACTION_UPDATE_ALL}, will
     * automatically add all widgets to the requested update queue.
     */
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // If requested, trigger update of all widgets
        if (ACTION_UPDATE_ALL.equals(intent.getAction())) {
            Log.d(TAG, "Requested UPDATE_ALL action");
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            requestUpdate(manager.getAppWidgetIds(new ComponentName(this, MedAppWidget.class)));
            requestUpdate(manager.getAppWidgetIds(new ComponentName(this, TinyAppWidget.class)));
        }

        // Only start processing thread if not already running
        synchronized (sLock) {
            if (!sThreadRunning) {
                sThreadRunning = true;
                new Thread(this).start();
            }
        }
    }

    /**
     * Main thread for running through any requested widget updates until none
     * remain. Also sets alarm to perform next update.
     */
    public void run() {
        Log.d(TAG, "Processing thread started");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ContentResolver resolver = getContentResolver();

        long now = System.currentTimeMillis();

        while (hasMoreUpdates()) {
            int appWidgetId = getNextUpdate();
            Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.CONTENT_URI, appWidgetId);

            // Check if widget is configured, and if we need to update cache
            Cursor cursor = null;
            boolean isConfigured = false;
            boolean shouldUpdate = false;

            try {
                cursor = resolver.query(appWidgetUri, PROJECTION_APPWIDGETS, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    isConfigured = cursor.getInt(COL_CONFIGURED) == AppWidgetsColumns.CONFIGURED_TRUE;

                    long lastUpdated = cursor.getLong(COL_LAST_UPDATED);
                    long deltaMinutes = (now - lastUpdated) / DateUtils.MINUTE_IN_MILLIS;
                    Log.d(TAG, "Delta since last forecast update is " + deltaMinutes + " min");
                    shouldUpdate = (Math.abs(now - lastUpdated) > FORECAST_CACHE_THROTTLE);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            if (!isConfigured) {
                // Skip this update if not configured yet
                Log.d(TAG, "Not configured yet, so skipping update");
                continue;
            } else if (shouldUpdate) {
                // Last update is outside throttle window, so update again
                try {
                    WebserviceHelper.updateForecasts(this, appWidgetUri, FORECAST_DAYS);
                } catch (ForecastParseException e) {
                    Log.e(TAG, "Problem parsing forecast", e);
                }
            }

            // Process this update through the correct provider
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            String providerName = info.provider.getClassName();
            RemoteViews updateViews = null;

            if (providerName.equals(MedAppWidget.class.getName())) {
                updateViews = MedAppWidget.buildUpdate(this, appWidgetUri);
            } else if (providerName.equals(TinyAppWidget.class.getName())) {
                updateViews = TinyAppWidget.buildUpdate(this, appWidgetUri);
            }

            // Push this update to surface
            if (updateViews != null) {
                appWidgetManager.updateAppWidget(appWidgetId, updateViews);
            }
        }

        // Schedule next update alarm, usually just before a 6-hour block. This
        // triggers updates at roughly 5:50AM, 11:50AM, 5:50PM, and 11:50PM.
        Time time = new Time();
        time.set(System.currentTimeMillis() + UPDATE_INTERVAL + UPDATE_TRIGGER_EARLY);
        time.hour -= (time.hour % 6);
        time.minute = 0;
        time.second = 0;

        long nextUpdate = time.toMillis(false) - UPDATE_TRIGGER_EARLY;
        long nowMillis = System.currentTimeMillis();

        // Throttle our updates just in case the math went funky
        if (nextUpdate - nowMillis < UPDATE_THROTTLE) {
            Log.d(TAG, "Calculated next update too early, throttling for a few minutes");
            nextUpdate = nowMillis + UPDATE_THROTTLE;
        }

        long deltaMinutes = (nextUpdate - nowMillis) / DateUtils.MINUTE_IN_MILLIS;
        Log.d(TAG, "Requesting next update at " + nextUpdate + ", in " + deltaMinutes + " min");

        Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
        updateIntent.setClass(this, UpdateService.class);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);

        // Schedule alarm, and force the device awake for this update
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);

        // No updates remaining, so stop service
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
