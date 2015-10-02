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

import android.app.Fragment;
import android.os.Bundle;

public class FragmentBuilder<F extends Fragment, B extends FragmentBuilder<F, B>> extends BaseFragmentBuilder<F, B> {
    protected FragmentBuilder(Class<F> fragmentClass, Class<B> builderClass) {
        super(fragmentClass, builderClass);
    }

    @Override
    protected void setArgs(F fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }
}
