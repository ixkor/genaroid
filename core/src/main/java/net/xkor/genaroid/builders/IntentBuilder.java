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

package net.xkor.genaroid.builders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentBuilder extends IntentBaseBuilder<IntentBuilder> {

    public IntentBuilder(Intent intent) {
        super(intent, IntentBuilder.class);
    }

    public IntentBuilder(Context context, Class<?> cls) {
        this(new Intent(context, cls));
    }

    public IntentBuilder() {
        this(new Intent());
    }

    public IntentBuilder(String action) {
        this(new Intent(action));
    }

    public IntentBuilder(String action, Uri uri) {
        this(new Intent(action, uri));
    }

    public IntentBuilder(String action, Uri uri, Context context, Class<?> cls) {
        this(new Intent(action, uri, context, cls));
    }
}
