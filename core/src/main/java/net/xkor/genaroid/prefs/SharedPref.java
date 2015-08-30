package net.xkor.genaroid.prefs;

import android.content.SharedPreferences;

public class SharedPref<T> {
    private final SharedPreferences preferences;
    private final String key;
    private T defValue;
    private final Class<T> prefClass;

    public SharedPref(SharedPreferences preferences, String key, T defValue, Class<T> prefClass) {
        this.preferences = preferences;
        this.key = key;
        this.defValue = defValue;
        this.prefClass = prefClass;
    }

    public T get() {
        if (prefClass.equals(Boolean.class)) {
            return prefClass.cast(preferences.getBoolean(key, (Boolean) defValue));
        } else if (prefClass.equals(String.class)) {
            return prefClass.cast(preferences.getString(key, (String) defValue));
        } else if (prefClass.equals(Integer.class)) {
            return prefClass.cast(preferences.getInt(key, (Integer) defValue));
        } else if (prefClass.equals(Float.class)) {
            return prefClass.cast(preferences.getFloat(key, (Float) defValue));
        } else if (prefClass.equals(Long.class)) {
            return prefClass.cast(preferences.getLong(key, (Long) defValue));
        } else {
            // TODO
        }
        return null;
    }

    public void set(T value) {
        // TODO
    }
}
