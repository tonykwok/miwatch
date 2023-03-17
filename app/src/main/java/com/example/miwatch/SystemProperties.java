package com.example.miwatch;

import android.util.Log;

/**
 * Mirrors hidden class {@link android.os.SystemProperties}.
 * @author weiqiang
 */
public class SystemProperties {
    private static final String TAG = "SystemProperties";

    private static final Class<?> SP = getSystemPropertiesClass();

    private SystemProperties() {
    }

    /**
     * Get the value for the given key.
     */
    public static String get(String key) {
        try {
            if (SP != null) {
                return (String) SP.getMethod("get", String.class).invoke(null, key);
            }
            return  null;
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting system property: ", e);
            return null;
        }
    }

    /**
     * Get the value for the given key.
     *
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     */
    public static String get(String key, String def) {
        try {
            if (SP != null) {
                return (String) SP.getMethod("get", String.class, String.class).invoke(null, key, def);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting system property: ", e);
            return def;
        }
    }

    /**
     * Get the value for the given key, returned as a boolean. Values 'n', 'no', '0', 'false' or
     * 'off' are considered false. Values 'y', 'yes', '1', 'true' or 'on' are considered true. (case
     * sensitive). If the key does not exist, or has any other value, then the default result is
     * returned.
     *
     * @param key
     *     the key to lookup
     * @param def
     *     a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is not able to be
     * parsed as a boolean.
     */
    public static boolean getBoolean(String key, boolean def) {
        try {
            if (SP != null) {
                return (Boolean) SP.getMethod("getBoolean", String.class, boolean.class).invoke(null, key, def);
            }else {
                return def;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting system property: ", e);
            return def;
        }
    }

    /**
     * Get the value for the given key, and return as an integer.
     *
     * @param key
     *     the key to lookup
     * @param def
     *     a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
     */
    public static int getInt(String key, int def) {
        try {
            if (SP != null) {
                return (Integer) SP.getMethod("getInt", String.class, int.class).invoke(null, key, def);
            } else {
                return def;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting system property: ", e);
            return def;
        }
    }

    /**
     * Get the value for the given key, and return as a long.
     *
     * @param key
     *     the key to lookup
     * @param def
     *     a default value to return
     * @return the key parsed as a long, or def if the key isn't found or cannot be parsed
     */
    public static long getLong(String key, long def) {
        try {
            return (Long) SP.getMethod("getLong", String.class, long.class).invoke(null, key, def);
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting system property: ", e);
            return def;
        }
    }

    /**
     * Get the value for the given key, and return as a float.
     *
     * @param key
     *     the key to lookup
     * @param def
     *     a default value to return
     * @return the key parsed as a float, or def if the key isn't found or cannot be parsed
     */
    public static float getFloat(String key, float def) {
        String value = get(key, String.valueOf(def));
        return Float.valueOf(value);
    }

    private static Class<?> getSystemPropertiesClass() {
        try {
            return Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException shouldNotHappen) {
            Log.e(TAG, "'android.os.SystemProperties' not found");
            return null;
        }
    }
}
