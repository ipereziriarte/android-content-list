/*
 * Copyright 2012 Olhe Ingenieros.
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

package com.olheingenieros.listexample.utils;

import android.util.Log;

import com.olheingenieros.listexample.BuildConfig;

/**
 * Helper methods that make logging more consistent throughout the app.
 */
public class LogUtils {
    private static final String LOG_PREFIX = "ListExample_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(final String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    public static Boolean isLogable() {
        return BuildConfig.DEBUG;
    }

    /**
     * WARNING: Don't use this when obfuscating class names with Proguard!
     */
    public static String makeLogTag(final Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, final String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(final String tag, final String message, final Throwable cause) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);
        }
    }

    public static void LOGV(final String tag, final String message) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message);
        }
    }

    public static void LOGV(final String tag, final String message, final Throwable cause) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message, cause);
        }
    }

    public static void LOGI(final String tag, final String message) {
        Log.i(tag, message);
    }

    public static void LOGI(final String tag, final String message, final Throwable cause) {
        Log.i(tag, message, cause);
    }

    public static void LOGW(final String tag, final String message) {
        Log.w(tag, message);
    }

    public static void LOGW(final String tag, final String message, final Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void LOGE(final String tag, final String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final String tag, final String message, final Throwable cause) {
        Log.e(tag, message, cause);
    }

    private LogUtils() {
    }
}
