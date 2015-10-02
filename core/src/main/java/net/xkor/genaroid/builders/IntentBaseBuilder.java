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

package net.xkor.genaroid.builders;

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

public class IntentBaseBuilder<T extends IntentBaseBuilder<T>> extends BundleBaseBuilder<T> {
    private Context context;
    private Intent intent;

    protected IntentBaseBuilder(Intent intent, Class<T> builderClass) {
        super(builderClass);
        this.intent = intent;
    }

    public T context(Context context) {
        this.context = context;
        return builderClass.cast(this);
    }

    public T action(String action) {
        intent.setAction(action);
        return builderClass.cast(this);
    }

    public T service(Class<? extends Service> service) {
        return setClass(service);
    }

    public T activity(Class<? extends Activity> activity) {
        return setClass(activity);
    }

    public T receiver(Class<? extends BroadcastReceiver> receiver) {
        return setClass(receiver);
    }

    public T component(ComponentName component) {
        intent.setComponent(component);
        return builderClass.cast(this);
    }

    public T className(Context context, String className) {
        intent.setClassName(context, className);
        this.context = context;
        return builderClass.cast(this);
    }

    public T className(String packageName, String className) {
        intent.setClassName(packageName, className);
        return builderClass.cast(this);
    }

    public T setPackage(String pack) {
        intent.setPackage(pack);
        return builderClass.cast(this);
    }

    public T flag(int flag) {
        return this.flags(flag);
    }

    public T flags(int... flags) {
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        return builderClass.cast(this);
    }

    public T extras(Bundle extras) {
        getBundle().putAll(extras);
        return builderClass.cast(this);
    }

    public T extras(Intent intent) {
        getBundle().putAll(intent.getExtras());
        return builderClass.cast(this);
    }

    public T data(Uri data) {
        intent.setData(data);
        return builderClass.cast(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public T dataNormalize(Uri data) {
        intent.setDataAndNormalize(data);
        return builderClass.cast(this);
    }

    public T type(String type) {
        intent.setType(type);
        return builderClass.cast(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public T typeNormalize(String type) {
        intent.setTypeAndNormalize(type);
        return builderClass.cast(this);
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

    private T setClass(Class<?> cls) {
        intent.setClass(context, cls);
        return builderClass.cast(this);
    }
}
