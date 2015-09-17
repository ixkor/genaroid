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

import java.lang.reflect.InvocationTargetException;

public abstract class BaseFragmentBuilder<T> extends BundleBuilder {
    private Class<T> fragmentClass;

    BaseFragmentBuilder(Class<T> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public T instantiate() {
        try {
            T fragment = fragmentClass.cast(fragmentClass.getConstructor().newInstance());
            setArgs(fragment, getBundle());
            return fragment;
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    protected abstract void setArgs(T fragment, Bundle bundle);

    public Bundle getArgs() {
        return getBundle();
    }
}
