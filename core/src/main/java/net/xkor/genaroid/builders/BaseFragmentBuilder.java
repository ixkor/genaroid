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

import android.os.Bundle;

public abstract class BaseFragmentBuilder<F, B extends BaseFragmentBuilder<F, B>> extends BundleBaseBuilder<B> {
    private Class<F> fragmentClass;

    protected BaseFragmentBuilder(Class<F> fragmentClass, Class<B> builderClass) {
        super(builderClass);
        this.fragmentClass = fragmentClass;
    }

    public F instantiate() {
        F fragment = null;
        try {
            fragment = fragmentClass.cast(fragmentClass.newInstance());
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        setArgs(fragment, getBundle());
        return fragment;
    }

    protected abstract void setArgs(F fragment, Bundle bundle);

    public Bundle getArgs() {
        return getBundle();
    }
}
