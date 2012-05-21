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

import java.io.IOException;
import java.util.List;

import org.jsharkey.sky.ForecastProvider.AppWidgets;
import org.jsharkey.sky.ForecastProvider.AppWidgetsColumns;

import android.app.Activity;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Activity to configure forecast widgets. Usually launched automatically by an
 * {@link AppWidgetHost} after the {@link AppWidgetManager#EXTRA_APPWIDGET_ID}
 * has been bound to a widget.
 */
public class ConfigureActivity extends Activity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    public static final String TAG = "ConfigureActivity";

    private Button mMap;
    private Button mSave;

    private EditText mTitle;
    private double mLat = Double.NaN;
    private double mLon = Double.NaN;
    private int mUnits = AppWidgetsColumns.UNITS_FAHRENHEIT;

    /**
     * Default zoom level when showing map to verify location.
     */
    private static final int ZOOM_LEVEL = 10;

    /**
     * Last found location fix, used when user selects "My current location."
     */
    private Location mLastFix;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    /**
     * Spawn a reverse geocoding operation to find names for the given
     * {@link Location}. Will update GUI when finished.
     */
    private void startGeocode(Location location) {
        new GeocoderTask().execute(new GeocodeQuery(location));
    }

    /**
     * Spawn a forward geocoding operation to find the location of a given name.
     * Will update GUI when finished.
     */
    private void startGeocode(String query) {
        new GeocoderTask().execute(new GeocodeQuery(query));
    }

    /**
     * Background task to perform a geocoding operation. Will disable GUI
     * actions while running in the background, and then update GUI with results
     * when found.
     * <p>
     * If no reverse geocoding results found, will still return original
     * coordinates but will leave suggested title empty.
     */
    private class GeocoderTask extends AsyncTask<GeocodeQuery, Void, GeocodeQuery> {
        private Geocoder mGeocoder;

        private GeocoderTask() {
            mGeocoder = new Geocoder(ConfigureActivity.this);
        }

        protected void onPreExecute() {
            // Show progress spinner and disable buttons
            setProgressBarIndeterminateVisibility(true);
            setActionEnabled(false);
        }

        protected GeocodeQuery doInBackground(GeocodeQuery... args) {
            GeocodeQuery query = args[0];
            GeocodeQuery result = null;

            try {
                if (!TextUtils.isEmpty(query.name)) {
                    // Forward geocode using query
                    List<Address> results = mGeocoder.getFromLocationName(query.name, 1);
                    if (results.size() > 0) {
                        result = new GeocodeQuery(results.get(0));
                    }
                } else if (!Double.isNaN(query.lat) && !Double.isNaN(query.lon)) {
                    // Reverse geocode using location
                    List<Address> results = mGeocoder.getFromLocation(query.lat, query.lon, 1);
                    if (results.size() > 0) {
                        result = new GeocodeQuery(results.get(0));
                        result.lat = query.lat;
                        result.lon = query.lon;
                    } else {
                        result = query;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Problem using geocoder", e);
            }

            return result;
        }

        protected void onPostExecute(GeocodeQuery found) {
            setProgressBarIndeterminateVisibility(false);

            // Update GUI with resolved string
            if (found == null) {
                mLat = Double.NaN;
                mLon = Double.NaN;
                setActionEnabled(false);
            } else {
                mTitle.setText(found.name);
                mLat = found.lat;
                mLon = found.lon;
                setActionEnabled(true);
            }
        }
    }

    /**
     * Temporary object to hold geocoding query and/or results.
     */
    private static class GeocodeQuery {
        String name = null;

        double lat = Double.NaN;

        double lon = Double.NaN;

        public GeocodeQuery(String query) {
            name = query;
        }

        public GeocodeQuery(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        /**
         * Summarize details of the given {@link Address}, walking down a
         * prioritized list of names until valid text is found to describe it.
         */
        public GeocodeQuery(Address address) {
            name = address.getLocality();
            if (name == null) {
                name = address.getFeatureName();
            }
            if (name == null) {
                name = address.getAdminArea();
            }
            if (name == null) {
                name = address.getPostalCode();
            }
            if (name == null) {
                name = address.getCountryName();
            }

            // Fill in coordinates, if given
            if (address.hasLatitude() && address.hasLongitude()) {
                lat = address.getLatitude();
                lon = address.getLongitude();
            }
        }
    }

    /**
     * Enable or disable any GUI actions, including text fields and buttons.
     */
    protected void setActionEnabled(boolean enabled) {
        mTitle.setEnabled(enabled);
        mMap.setEnabled(enabled);
        mSave.setEnabled(enabled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.configure);

        mTitle = (EditText)findViewById(R.id.conf_title);

        ((RadioGroup)findViewById(R.id.conf_units)).setOnCheckedChangeListener(this);

        ((RadioButton)findViewById(R.id.conf_current)).setOnClickListener(this);
        ((RadioButton)findViewById(R.id.conf_manual)).setOnClickListener(this);

        mMap = (Button)findViewById(R.id.conf_map);
        mSave = (Button)findViewById(R.id.conf_save);

        mMap.setOnClickListener(this);
        mSave.setOnClickListener(this);

        // Read the appWidgetId to configure from the incoming intent
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setConfigureResult(Activity.RESULT_CANCELED);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // TODO: handle editing an existing widget by reading values

        // If restoring, read location and units from bundle
        if (savedInstanceState != null) {
            mLat = savedInstanceState.getDouble(AppWidgetsColumns.LAT);
            mLon = savedInstanceState.getDouble(AppWidgetsColumns.LON);
            mUnits = savedInstanceState.getInt(AppWidgetsColumns.UNITS);
            if (mUnits == AppWidgetsColumns.UNITS_FAHRENHEIT) {
                ((RadioButton)findViewById(R.id.conf_units_f)).setSelected(true);
            } else if (mUnits == AppWidgetsColumns.UNITS_CELSIUS) {
                ((RadioButton)findViewById(R.id.conf_units_c)).setSelected(true);
            }
        }

        // Start listener to find current location
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (provider != null) {
            // Fire off geocoding request for last fix, but only if not
            // restoring
            mLastFix = locationManager.getLastKnownLocation(provider);
            if (mLastFix != null && savedInstanceState == null) {
                startGeocode(mLastFix);
            }
        }

        if (mLastFix == null) {
            // No enabled providers found, so disable option
            RadioButton radioCurrent = (RadioButton)findViewById(R.id.conf_current);
            radioCurrent.setVisibility(View.GONE);

            mTitle.setText(R.string.conf_nofix);
        }
    }

    /**
     * Handle any new intents wrapping around from {@link SearchManager}.
     */
    @Override
    public void onNewIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // Fire off geocoding request for given query
            String query = intent.getStringExtra(SearchManager.QUERY);
            startGeocode(query);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(AppWidgetsColumns.LAT, mLat);
        outState.putDouble(AppWidgetsColumns.LON, mLon);
        outState.putInt(AppWidgetsColumns.UNITS, mUnits);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conf_current: {
                // Picked current location, start geocode to find location name
                startGeocode(mLastFix);
                break;
            }
            case R.id.conf_manual: {
                // Picked manual search, so trigger search dialog
                onSearchRequested();
                break;
            }
            case R.id.conf_map: {
                // Picked verify on map, so launch mapping intent
                Uri mapUri = Uri.parse(String.format("geo:%f,%f?z=%d", mLat, mLon, ZOOM_LEVEL));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                mapIntent.setData(mapUri);

                startActivity(mapIntent);
                break;
            }
            case R.id.conf_save: {
                // Picked save, so write values to backend
                ContentValues values = new ContentValues();
                String title = mTitle.getText().toString();
                values.put(BaseColumns._ID, mAppWidgetId);
                values.put(AppWidgetsColumns.TITLE, title);
                values.put(AppWidgetsColumns.LAT, mLat);
                values.put(AppWidgetsColumns.LON, mLon);
                values.put(AppWidgetsColumns.UNITS, mUnits);
                values.put(AppWidgetsColumns.LAST_UPDATED, -1);
                values.put(AppWidgetsColumns.CONFIGURED, AppWidgetsColumns.CONFIGURED_TRUE);

                // TODO: update instead of insert if editing an existing widget
                ContentResolver resolver = getContentResolver();
                resolver.insert(AppWidgets.CONTENT_URI, values);

                // Trigger pushing a widget update to surface
                UpdateService.requestUpdate(new int[] {
                    mAppWidgetId
                });
                startService(new Intent(this, UpdateService.class));

                setConfigureResult(Activity.RESULT_OK);
                finish();

                break;
            }
        }
    }

    /**
     * Change {@link #mUnits} when requested by user.
     */
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.conf_units_f:
                mUnits = AppWidgetsColumns.UNITS_FAHRENHEIT;
                break;
            case R.id.conf_units_c:
                mUnits = AppWidgetsColumns.UNITS_CELSIUS;
                break;
        }
    }

    /**
     * Convenience method to always include {@link #mAppWidgetId} when setting
     * the result {@link Intent}.
     */
    public void setConfigureResult(int resultCode) {
        final Intent data = new Intent();
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(resultCode, data);
    }
}
