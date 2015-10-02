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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import net.xkor.genaroid.internal.Bindable;
import net.xkor.genaroid.internal.Parameterizable;
import net.xkor.genaroid.internal.Restorable;

public final class Genaroid {
    public static void bind(Object object, View rootView) {
        if (object instanceof Bindable) {
            ((Bindable) object)._gen_bind(rootView);
        }
    }

    public static void bind(Activity activity) {
        if (activity instanceof Bindable) {
            ((Bindable) activity)._gen_bind(activity.findViewById(android.R.id.content));
        }
    }

    public static void unbind(Object object) {
        if (object instanceof Bindable) {
            ((Bindable) object)._gen_unbind();
        }
    }

    public static void saveInstanceState(Object object, Bundle outState) {
        if (object instanceof Restorable) {
            ((Restorable) object)._gen_saveInstanceState(outState);
        }
    }

    public static void restoreInstanceState(Object object, Bundle savedState) {
        if (object instanceof Restorable && savedState != null) {
            ((Restorable) object)._gen_restoreInstanceState(savedState);
        }
    }

    public static void readParams(Object object, Bundle params) {
        if (object instanceof Parameterizable) {
            ((Parameterizable) object)._gen_readParams(params);
        }
    }

    public static void readParams(Activity activity) {
        if (activity instanceof Parameterizable && activity.getIntent() != null) {
            ((Parameterizable) activity)._gen_readParams(activity.getIntent().getExtras());
        }
    }

    public static void readParams(Fragment fragment) {
        if (fragment instanceof Parameterizable && fragment.getArguments() != null) {
            ((Parameterizable) fragment)._gen_readParams(fragment.getArguments());
        }
    }

    public static void readParams(android.support.v4.app.Fragment fragment) {
        if (fragment instanceof Parameterizable && fragment.getArguments() != null) {
            ((Parameterizable) fragment)._gen_readParams(fragment.getArguments());
        }
    }
}
