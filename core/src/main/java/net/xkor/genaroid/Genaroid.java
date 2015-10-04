/*
 * Copyright (C) 2015 Aleksei Skoriatin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xkor.genaroid;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import net.xkor.genaroid.internal.Bindable;
import net.xkor.genaroid.internal.Parameterizable;
import net.xkor.genaroid.internal.Restorable;

public final class Genaroid {
    public static void bind(@NonNull Object object, @NonNull View rootView) {
        if (object instanceof Bindable) {
            ((Bindable) object)._gen_bind(rootView);
        }
    }

    public static void bind(@NonNull Activity activity) {
        bind(activity, activity.findViewById(android.R.id.content));
    }

    public static void unbind(@NonNull Object object) {
        if (object instanceof Bindable) {
            ((Bindable) object)._gen_unbind();
        }
    }

    public static void saveInstanceState(@NonNull Object object, @NonNull Bundle outState) {
        if (object instanceof Restorable) {
            ((Restorable) object)._gen_saveInstanceState(outState);
        }
    }

    public static void restoreInstanceState(@NonNull Object object, @Nullable Bundle savedState) {
        if (object instanceof Restorable && savedState != null) {
            ((Restorable) object)._gen_restoreInstanceState(savedState);
        }
    }

    public static void readParams(@NonNull Object object, @Nullable Bundle params) {
        if (object instanceof Parameterizable && params != null) {
            ((Parameterizable) object)._gen_readParams(params);
        }
    }

    public static void readParams(@NonNull Activity activity) {
        if (activity.getIntent() != null) {
            readParams(activity, activity.getIntent().getExtras());
        }
    }

    public static void readParams(@NonNull Fragment fragment) {
        readParams(fragment, fragment.getArguments());
    }

    public static void readParams(@NonNull android.support.v4.app.Fragment fragment) {
        readParams(fragment, fragment.getArguments());
    }
}
