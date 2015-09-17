/**
 * Copyright (C) 2015 Aleksei Skoriatin
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xkor.genaroid;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class BundleBuilder {
    private Bundle bundle = new Bundle();

    public BundleBuilder put(String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleBuilder put(String key, byte value) {
        bundle.putByte(key, value);
        return this;
    }

    public BundleBuilder put(String key, char value) {
        bundle.putChar(key, value);
        return this;
    }

    public BundleBuilder put(String key, double value) {
        bundle.putDouble(key, value);
        return this;
    }

    public BundleBuilder put(String key, float value) {
        bundle.putFloat(key, value);
        return this;
    }

    public BundleBuilder put(String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleBuilder put(String key, long value) {
        bundle.putLong(key, value);
        return this;
    }

    public BundleBuilder put(String key, short value) {
        bundle.putShort(key, value);
        return this;
    }

    public BundleBuilder put(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleBuilder put(String key, byte[] value) {
        bundle.putByteArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, boolean[] value) {
        bundle.putBooleanArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, char[] value) {
        bundle.putCharArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, double[] value) {
        bundle.putDoubleArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, float[] value) {
        bundle.putFloatArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, int[] value) {
        bundle.putIntArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, long[] value) {
        bundle.putLongArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, short[] value) {
        bundle.putShortArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, Bundle value) {
        bundle.putBundle(key, value);
        return this;
    }

    public BundleBuilder put(String key, CharSequence value) {
        bundle.putCharSequence(key, value);
        return this;
    }

    public BundleBuilder put(String key, Parcelable value) {
        bundle.putParcelable(key, value);
        return this;
    }

    public BundleBuilder put(String key, Serializable value) {
        bundle.putSerializable(key, value);
        return this;
    }

    public BundleBuilder put(String key, CharSequence[] value) {
        bundle.putCharSequenceArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, Parcelable[] value) {
        bundle.putParcelableArray(key, value);
        return this;
    }

    public BundleBuilder put(String key, String[] value) {
        bundle.putStringArray(key, value);
        return this;
    }

    public BundleBuilder putCharSequenceList(String key, ArrayList<CharSequence> value) {
        bundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public BundleBuilder putIntegerList(String key, ArrayList<Integer> value) {
        bundle.putIntegerArrayList(key, value);
        return this;
    }

    public BundleBuilder putParcelableList(String key, ArrayList<? extends Parcelable> value) {
        bundle.putParcelableArrayList(key, value);
        return this;
    }

    public BundleBuilder putStringList(String key, ArrayList<String> value) {
        bundle.putStringArrayList(key, value);
        return this;
    }

    public Bundle getBundle() {
        return bundle;
    }

    protected void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
