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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsharkey.sky.ForecastProvider.AppWidgets;
import org.jsharkey.sky.ForecastProvider.AppWidgetsColumns;
import org.jsharkey.sky.ForecastProvider.Forecasts;
import org.jsharkey.sky.ForecastProvider.ForecastsColumns;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

/**
 * Helper class to handle querying a webservice for forecast details and parsing
 * results into {@link ForecastProvider}.
 */
public class WebserviceHelper {
    private static final String TAG = "ForcastHelper";

    private static final String[] PROJECTION_APPWIDGET = {
        AppWidgetsColumns.LAT,
        AppWidgetsColumns.LON,
    };

    private static final int COL_LAT = 0;
    private static final int COL_LON = 1;

    static final boolean FAKE_DATA = false;

    static final String WEBSERVICE_URL = "http://www.weather.gov/forecasts/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php?&lat=%f&lon=%f&format=24+hourly&numDays=%d";

    /**
     * Timeout to wait for webservice to respond. Because we're in the
     * background, we don't mind waiting for good data.
     */
    static final long WEBSERVICE_TIMEOUT = 30 * DateUtils.SECOND_IN_MILLIS;

    /**
     * Various XML tags present in the response.
     */
    private static final String TAG_TEMPERATURE = "temperature";
    private static final String TAG_WEATHER = "weather";
    private static final String TAG_POP = "probability-of-precipitation";
    private static final String TAG_HAZARDS = "hazards";
    private static final String TAG_WEATHER_CONDITIONS = "weather-conditions";
    private static final String TAG_HAZARD = "hazard";
    private static final String TAG_LAYOUT_KEY = "layout-key";
    private static final String TAG_START_VALID_TIME = "start-valid-time";
    private static final String TAG_VALUE = "value";
    private static final String TAG_HAZARDTEXTURL = "hazardTextURL";
    private static final String TAG_MOREWEATHERINFORMATION = "moreWeatherInformation";

    /**
     * Various XML attributes present in the response.
     */
    private static final String ATTR_TIME_LAYOUT = "time-layout";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_WEATHER_SUMMARY = "weather-summary";
    private static final String ATTR_PHENOMENA = "phenomena";
    private static final String ATTR_SIGNIFICANCE = "significance";

    private static final String TYPE_MAXIMUM = "maximum";
    private static final String TYPE_MINIMUM = "minimum";

    private static final String EXAMPLE_RESPONSE = "<?xml version=\"1.0\"?><dwml version=\"1.0\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.nws.noaa.gov/forecasts/xml/DWMLgen/schema/DWML.xsd\"><head><product srsName=\"WGS 1984\" concise-name=\"dwmlByDay\" operational-mode=\"official\"><title>NOAA's National Weather Service Forecast by 24 Hour Period</title><field>meteorological</field><category>forecast</category><creation-date refresh-frequency=\"PT1H\">2009-03-22T22:56:32Z</creation-date></product><source><more-information>http://www.nws.noaa.gov/forecasts/xml/</more-information><production-center>Meteorological Development Laboratory<sub-center>Product Generation Branch</sub-center></production-center><disclaimer>http://www.nws.noaa.gov/disclaimer.html</disclaimer><credit>http://www.weather.gov/</credit><credit-logo>http://www.weather.gov/images/xml_logo.gif</credit-logo><feedback>http://www.weather.gov/feedback.php</feedback></source></head><data><location><location-key>point1</location-key><point latitude=\"39.91\" longitude=\"-121.29\"/></location><moreWeatherInformation applicable-location=\"point1\">http://forecast.weather.gov/MapClick.php?textField1=39.91&amp;textField2=-121.29</moreWeatherInformation><time-layout time-coordinate=\"local\" summarization=\"24hourly\"><layout-key>k-p24h-n4-1</layout-key><start-valid-time>2009-03-22T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-23T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-23T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-24T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-24T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-25T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-25T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-26T06:00:00-07:00</end-valid-time></time-layout><time-layout time-coordinate=\"local\" summarization=\"12hourly\"><layout-key>k-p12h-n8-2</layout-key><start-valid-time>2009-03-22T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-22T18:00:00-07:00</end-valid-time><start-valid-time>2009-03-22T18:00:00-07:00</start-valid-time><end-valid-time>2009-03-23T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-23T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-23T18:00:00-07:00</end-valid-time><start-valid-time>2009-03-23T18:00:00-07:00</start-valid-time><end-valid-time>2009-03-24T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-24T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-24T18:00:00-07:00</end-valid-time><start-valid-time>2009-03-24T18:00:00-07:00</start-valid-time><end-valid-time>2009-03-25T06:00:00-07:00</end-valid-time><start-valid-time>2009-03-25T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-25T18:00:00-07:00</end-valid-time><start-valid-time>2009-03-25T18:00:00-07:00</start-valid-time><end-valid-time>2009-03-26T06:00:00-07:00</end-valid-time></time-layout><time-layout time-coordinate=\"local\" summarization=\"24hourly\"><layout-key>k-p4d-n1-3</layout-key><start-valid-time>2009-03-22T06:00:00-07:00</start-valid-time><end-valid-time>2009-03-26T06:00:00-07:00</end-valid-time></time-layout><parameters applicable-location=\"point1\"><temperature type=\"maximum\" units=\"Fahrenheit\" time-layout=\"k-p24h-n4-1\"><name>Daily Maximum Temperature</name><value>32</value><value>47</value><value>55</value><value>58</value></temperature><temperature type=\"minimum\" units=\"Fahrenheit\" time-layout=\"k-p24h-n4-1\"><name>Daily Minimum Temperature</name><value>24</value><value>28</value><value>32</value><value>31</value></temperature><probability-of-precipitation type=\"12 hour\" units=\"percent\" time-layout=\"k-p12h-n8-2\"><name>12 Hourly Probability of Precipitation</name><value>98</value><value>22</value><value>6</value><value>6</value><value>4</value><value>0</value><value>16</value><value>18</value></probability-of-precipitation><weather time-layout=\"k-p24h-n4-1\"><name>Weather Type, Coverage, and Intensity</name><weather-conditions weather-summary=\"Slight Chance Snow Showers\"><value coverage=\"slight chance\" intensity=\"light\" weather-type=\"snow showers\" qualifier=\"none\"/></weather-conditions><weather-conditions weather-summary=\"Partly Cloudy\"/><weather-conditions weather-summary=\"Mostly Sunny\"/><weather-conditions weather-summary=\"Partly Cloudy\"/></weather><conditions-icon type=\"forecast-NWS\" time-layout=\"k-p24h-n4-1\"><name>Conditions Icons</name><icon-link>http://www.nws.noaa.gov/weather/images/fcicons/sn100.jpg</icon-link><icon-link>http://www.nws.noaa.gov/weather/images/fcicons/sct.jpg</icon-link><icon-link>http://www.nws.noaa.gov/weather/images/fcicons/few.jpg</icon-link><icon-link>http://www.nws.noaa.gov/weather/images/fcicons/sct.jpg</icon-link></conditions-icon><hazards time-layout=\"k-p4d-n1-3\"><name>Watches, Warnings, and Advisories</name><hazard-conditions><hazard hazardCode=\"LW.Y\" phenomena=\"Lake Wind\" significance=\"Advisory\" hazardType=\"long duration\"><hazardTextURL>http://forecast.weather.gov/wwamap/wwatxtget.php?cwa=usa&amp;wwa=Lake%20Wind%20Advisory</hazardTextURL></hazard></hazard-conditions></hazards></parameters></data></dwml>";

    /**
     * Recycled string builder used by {@link #parseDate(String)}.
     */
    private static Editable sEditable = new SpannableStringBuilder();

    /**
     * Recycled timestamp used by {@link #parseDate(String)}.
     */
    private static Time sTime = new Time();

    /**
     * Exception to inform callers that we ran into problems while parsing the
     * forecast returned by the webservice.
     */
    public static final class ForecastParseException extends Exception {
        public ForecastParseException(String detailMessage) {
            super(detailMessage);
        }

        public ForecastParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

    /**
     * Parse a NWS date string into a Unix timestamp. Assumes incoming values
     * are in the format "2009-03-23T18:00:00-07:00", which we adjust slightly
     * to correctly follow RFC 3339 before parsing.
     */
    private static long parseDate(String raw) throws TimeFormatException {
        // Inject milliseconds so that NWS dates follow RFC
        sEditable.clear();
        sEditable.append(raw);
        sEditable.insert(19, ".000");

        String rfcFormat = sEditable.toString();
        sTime.parse3339(rfcFormat);
        return sTime.toMillis(false);
    }

    /**
     * Class holding a specific forecast at a point in time.
     */
    private static class Forecast {
        boolean alert = false;
        long validStart = Long.MIN_VALUE;
        int tempHigh = Integer.MIN_VALUE;
        int tempLow = Integer.MIN_VALUE;
        String conditions;
        String url;
    }

    /**
     * Retrieve a specific {@link Forecast} object from the given {@link Map}
     * structure. If the {@link Forecast} doesn't exist, it's created and
     * returned.
     */
    private static Forecast getForecast(Map<String, List<Forecast>> forecasts,
            String layout, int index) {
        if (!forecasts.containsKey(layout)) {
            forecasts.put(layout, new ArrayList<Forecast>());
        }
        List<Forecast> layoutSpecific = forecasts.get(layout);

        while (index >= layoutSpecific.size()) {
            layoutSpecific.add(new Forecast());
        }
        return layoutSpecific.get(index);
    }

    /**
     * Flatten a set of {@link Forecast} objects that are separated into
     * <code>time-layout</code> sections in the given {@link Map}. This discards
     * any forecasts that have empty {@link Forecast#conditions}.
     * <p>
     * Sorts the resulting list by time, with any alerts forced to the top.
     */
    private static List<Forecast> flattenForecasts(Map<String, List<Forecast>> forecasts) {
        List<Forecast> flat = new ArrayList<Forecast>();

        // Collect together all forecasts that have valid conditions
        for (String layout : forecasts.keySet()) {
            for (Forecast forecast : forecasts.get(layout)) {
                if (!TextUtils.isEmpty(forecast.conditions)) {
                    flat.add(forecast);
                }
            }
        }

        // Sort by time, but always bump alerts to top
        Collections.sort(flat, new Comparator<Forecast>() {
            public int compare(Forecast left, Forecast right) {
                if (left.alert) {
                    return -1;
                } else {
                    return (int)(left.validStart - right.validStart);
                }
            }
        });

        return flat;
    }

    /**
     * Perform a webservice query to retrieve and store the forecast for the
     * given widget. This call blocks until request is finished and
     * {@link Forecasts#CONTENT_URI} has been updated.
     */
    public static void updateForecasts(Context context, Uri appWidgetUri, int days)
            throws ForecastParseException {

        Uri appWidgetForecasts = Uri.withAppendedPath(appWidgetUri, AppWidgets.TWIG_FORECASTS);

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = null;
        double lat = Double.NaN;
        double lon = Double.NaN;

        // Pull exact forecast location from database
        try {
            cursor = resolver.query(appWidgetUri, PROJECTION_APPWIDGET, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                lat = cursor.getDouble(COL_LAT);
                lon = cursor.getDouble(COL_LON);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Query webservice for this location
        List<Forecast> forecasts = queryLocation(lat, lon, days);

        if (forecasts == null || forecasts.size() == 0) {
            throw new ForecastParseException("No forecasts found from webservice query");
        }

        // Purge existing forecasts covered by incoming data, and anything
        // before today
        long lastMidnight = ForecastUtils.getLastMidnight();
        long earliest = Long.MAX_VALUE;
        for (Forecast forecast : forecasts) {
            earliest = Math.min(earliest, forecast.validStart);
        }

        resolver.delete(appWidgetForecasts,
            ForecastsColumns.VALID_START + " >= " + earliest + " OR " +
            ForecastsColumns.VALID_START + " <= " + lastMidnight, null);

        // Insert any new forecasts found
        ContentValues values = new ContentValues();
        for (Forecast forecast : forecasts) {
            Log.d(TAG, "inserting forecast with validStart=" + forecast.validStart);
            values.clear();
            values.put(ForecastsColumns.VALID_START, forecast.validStart);
            values.put(ForecastsColumns.TEMP_HIGH, forecast.tempHigh);
            values.put(ForecastsColumns.TEMP_LOW, forecast.tempLow);
            values.put(ForecastsColumns.CONDITIONS, forecast.conditions);
            values.put(ForecastsColumns.URL, forecast.url);
            if (forecast.alert) {
                values.put(ForecastsColumns.ALERT, ForecastsColumns.ALERT_TRUE);
            }
            resolver.insert(appWidgetForecasts, values);
        }

        // Mark widget cache as being updated
        values.clear();
        values.put(AppWidgetsColumns.LAST_UPDATED, System.currentTimeMillis());
        resolver.update(appWidgetUri, values, null, null);
    }

    /**
     * Query the given location and parse any returned data into a set of
     * {@link Forecast} objects. This is a blocking call while waiting for the
     * webservice to return.
     */
    private static List<Forecast> queryLocation(double lat, double lon, int days)
            throws ForecastParseException {

        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            throw new ForecastParseException("Requested forecast for invalid location");
        } else {
            Log.d(TAG, String.format("queryLocation() with lat=%f, lon=%f, days=%d", lat, lon, days));
        }

        Reader responseReader;
        List<Forecast> forecasts = null;

        if (FAKE_DATA) {
            // Feed back fake data, if requested
            responseReader = new StringReader(EXAMPLE_RESPONSE);

        } else {
            // Perform webservice query and parse result
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(String.format(WEBSERVICE_URL, lat, lon, days));

            try {
                HttpResponse response = client.execute(request);

                StatusLine status = response.getStatusLine();
                Log.d(TAG, "Request returned status " + status);

                HttpEntity entity = response.getEntity();
                responseReader = new InputStreamReader(entity.getContent());

            } catch (IOException e) {
                throw new ForecastParseException("Problem calling forecast API", e);
            }
        }

        // If response found, send through to parser
        if (responseReader != null) {
            forecasts = parseResponse(responseReader);
        }

        return forecasts;
    }

    /**
     * Parse a webservice XML response into {@link Forecast} objects.
     */
    private static List<Forecast> parseResponse(Reader response)
            throws ForecastParseException {
        // Keep a temporary mapping between time series tags and forecasts
        Map<String, List<Forecast>> forecasts = new HashMap<String, List<Forecast>>();
        String detailsUrl = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            int index = 0;
            String thisTag = null;
            String thisLayout = null;
            String thisType = null;

            xpp.setInput(response);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    thisTag = xpp.getName();

                    if (TAG_TEMPERATURE.equals(thisTag) || TAG_WEATHER.equals(thisTag)
                            || TAG_POP.equals(thisTag) || TAG_HAZARDS.equals(thisTag)) {
                        thisLayout = xpp.getAttributeValue(null, ATTR_TIME_LAYOUT);
                        thisType = xpp.getAttributeValue(null, ATTR_TYPE);
                        index = -1;

                    } else if (TAG_WEATHER_CONDITIONS.equals(thisTag)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, ++index);
                        forecast.conditions = xpp.getAttributeValue(null, ATTR_WEATHER_SUMMARY);

                    } else if (TAG_HAZARD.equals(thisTag)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, ++index);
                        forecast.alert = true;
                        forecast.conditions = xpp.getAttributeValue(null, ATTR_PHENOMENA) + " "
                                + xpp.getAttributeValue(null, ATTR_SIGNIFICANCE);
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    thisTag = null;

                } else if (eventType == XmlPullParser.TEXT) {
                    if (TAG_LAYOUT_KEY.equals(thisTag)) {
                        thisLayout = xpp.getText();
                        index = -1;

                    } else if (TAG_START_VALID_TIME.equals(thisTag)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, ++index);
                        forecast.validStart = parseDate(xpp.getText());

                    } else if (TAG_VALUE.equals(thisTag) && TYPE_MAXIMUM.equals(thisType)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, ++index);
                        forecast.tempHigh = Integer.parseInt(xpp.getText());
                        forecast.url = detailsUrl;

                    } else if (TAG_VALUE.equals(thisTag) && TYPE_MINIMUM.equals(thisType)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, ++index);
                        forecast.tempLow = Integer.parseInt(xpp.getText());

                    } else if (TAG_HAZARDTEXTURL.equals(thisTag)) {
                        Forecast forecast = getForecast(forecasts, thisLayout, index);
                        forecast.url = xpp.getText();

                    } else if (TAG_MOREWEATHERINFORMATION.equals(thisTag)) {
                        detailsUrl = xpp.getText();

                    }
                }
                eventType = xpp.next();
            }
        } catch (IOException e) {
            throw new ForecastParseException("Problem parsing XML forecast", e);
        } catch (XmlPullParserException e) {
            throw new ForecastParseException("Problem parsing XML forecast", e);
        } catch (TimeFormatException e) {
            throw new ForecastParseException("Problem parsing XML forecast", e);
        }

        // Flatten non-empty forecasts into single list
        return flattenForecasts(forecasts);
    }
}
