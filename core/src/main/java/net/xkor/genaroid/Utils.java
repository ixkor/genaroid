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

import android.os.Parcelable;

import java.lang.reflect.Array;

public final class Utils {
    @SuppressWarnings("unchecked")
    public static <T extends Parcelable> T[] castParcelableArray(Class<T> clazz, Parcelable[] parcelableArray) {
        final int length = parcelableArray.length;
        final T[] array = (T[]) Array.newInstance(clazz, length);
        for (int i = 0; i < length; i++) {
            array[i] = (T) parcelableArray[i];
        }
        return array;
    }
}
