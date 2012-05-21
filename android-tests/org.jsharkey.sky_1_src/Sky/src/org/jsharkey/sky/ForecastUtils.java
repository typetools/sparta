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

import java.util.regex.Pattern;

import org.jsharkey.sky.ForecastProvider.AppWidgetsColumns;
import org.jsharkey.sky.ForecastProvider.ForecastsColumns;

import android.content.res.Resources;
import android.text.format.Time;

/**
 * Various forecast utilities.
 */
public class ForecastUtils {
    /**
     * Time when we consider daytime to begin. We keep this early to make sure
     * that our 6AM widget update will change icons correctly.
     */
    private static final int DAYTIME_BEGIN_HOUR = 5;

    /**
     * Time when we consider daytime to end. We keep this early to make sure
     * that our 6PM widget update will change icons correctly.
     */
    private static final int DAYTIME_END_HOUR = 16;

    private static final Pattern sIconAlert = Pattern.compile("(alert|advisory|warning|watch)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconStorm = Pattern.compile("(thunder|tstms)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconSnow = Pattern.compile("(snow|ice|frost|flurries)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconShower = Pattern.compile("(rain)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconScatter = Pattern.compile("(shower|drizzle)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconClear = Pattern.compile("(sunny|breezy|clear)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sIconClouds = Pattern.compile("(cloud|fog)", Pattern.CASE_INSENSITIVE);

    /**
     * Select an icon to describe the given {@link ForecastsColumns#CONDITIONS}
     * string. Uses a descending importance scale that matches keywords against
     * the described conditions.
     * 
     * @param daytime If true, return daylight-specific icons when available,
     *            otherwise assume night icons.
     */
    public static int getIconForForecast(String conditions, boolean daytime) {
        int icon = 0;
        if (sIconAlert.matcher(conditions).find()) {
            icon = R.drawable.weather_severe_alert;
        } else if (sIconStorm.matcher(conditions).find()) {
            icon = R.drawable.weather_storm;
        } else if (sIconSnow.matcher(conditions).find()) {
            icon = R.drawable.weather_snow;
        } else if (sIconShower.matcher(conditions).find()) {
            icon = R.drawable.weather_showers;
        } else if (sIconScatter.matcher(conditions).find()) {
            icon = R.drawable.weather_showers_scattered;
        } else if (sIconClear.matcher(conditions).find()) {
            icon = daytime ? R.drawable.weather_clear : R.drawable.weather_clear_night;
        } else if (sIconClouds.matcher(conditions).find()) {
            icon = daytime ? R.drawable.weather_few_clouds : R.drawable.weather_few_clouds_night;
        }
        return icon;
    }

    /**
     * Correctly format the given temperature for user display.
     * 
     * @param temp Temperature to format, in degrees Fahrenheit.
     * @param units Target units to convert to before displaying, usually
     *            something like {@link AppWidgetsColumns#UNITS_FAHRENHEIT}.
     */
    public static String formatTemp(Resources res, int temp, int units) {
        if (units == AppWidgetsColumns.UNITS_FAHRENHEIT) {
            return res.getString(R.string.temperature_f, temp);
        } else if (units == AppWidgetsColumns.UNITS_CELSIUS) {
            // Convert to Celsius before display
            temp = ((temp - 32) * 5) / 9;
            return res.getString(R.string.temperature_c, temp);
        }
        return null;
    }

    /**
     * Get the timestamp of the last midnight, in a base similar to
     * {@link System#currentTimeMillis()}.
     */
    public static long getLastMidnight() {
        Time time = new Time();
        time.setToNow();
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        return time.toMillis(false);
    }

    /**
     * Calcuate if it's currently "daytime" by our internal definition. Used to
     * decide which icons to show when updating widgets.
     */
    public static boolean isDaytime() {
        Time time = new Time();
        time.setToNow();
        return (time.hour >= DAYTIME_BEGIN_HOUR && time.hour <= DAYTIME_END_HOUR);
    }
}
