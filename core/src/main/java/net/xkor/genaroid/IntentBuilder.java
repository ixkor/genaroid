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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class IntentBuilder extends BundleBuilder {
    private Context context;
    private Intent intent;

    public IntentBuilder(Context context, Class<?> cls) {
        intent = new Intent(context, cls);
        this.context = context;
    }

    public IntentBuilder() {
        intent = new Intent();
    }

    public IntentBuilder(Intent intent) {
        this.intent = intent;
    }

    public IntentBuilder(String action) {
        intent = new Intent(action);
    }

    public IntentBuilder(String action, Uri uri) {
        intent = new Intent(action, uri);
    }

    public IntentBuilder(String action, Uri uri, Context context, Class<?> cls) {
        intent = new Intent(action, uri, context, cls);
        this.context = context;
    }

    public IntentBuilder context(Context context) {
        this.context = context;
        return this;
    }

    public IntentBuilder action(String action) {
        intent.setAction(action);
        return this;
    }

    public IntentBuilder service(Class<? extends Service> service) {
        return setClass(service);
    }

    public IntentBuilder activity(Class<? extends Activity> activity) {
        return setClass(activity);
    }

    public IntentBuilder receiver(Class<? extends BroadcastReceiver> receiver) {
        return setClass(receiver);
    }

    public IntentBuilder component(ComponentName component) {
        intent.setComponent(component);
        return this;
    }

    public IntentBuilder className(Context context, String className) {
        intent.setClassName(context, className);
        this.context = context;
        return this;
    }

    public IntentBuilder className(String packageName, String className) {
        intent.setClassName(packageName, className);
        return this;
    }

    public IntentBuilder setPackage(String pack) {
        intent.setPackage(pack);
        return this;
    }

    public IntentBuilder flag(int flag) {
        return this.flags(flag);
    }

    public IntentBuilder flags(int... flags) {
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        return this;
    }

    public IntentBuilder extras(Bundle extras) {
        getBundle().putAll(extras);
        return this;
    }

    public IntentBuilder extras(Intent intent) {
        getBundle().putAll(intent.getExtras());
        return this;
    }

    public IntentBuilder data(Uri data) {
        intent.setData(data);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public IntentBuilder dataNormalize(Uri data) {
        intent.setDataAndNormalize(data);
        return this;
    }

    public IntentBuilder type(String type) {
        intent.setType(type);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public IntentBuilder typeNormalize(String type) {
        intent.setTypeAndNormalize(type);
        return this;
    }

    public void start() {
        context.startActivity(getIntent());
    }

    public void startForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(getIntent(), requestCode);
    }

    public void startForResult(int requestCode) {
        if (context instanceof Activity) {
            startForResult((Activity) context, requestCode);
        } else {
            throw new IllegalArgumentException("Method startForResult(int requestCode) can not be called when context parameter is not Activity.");
        }
    }

    public Intent getIntent() {
        intent.putExtras(getBundle());
        return intent;
    }

    private IntentBuilder setClass(Class<?> cls) {
        intent.setClass(context, cls);
        return this;
    }
}
