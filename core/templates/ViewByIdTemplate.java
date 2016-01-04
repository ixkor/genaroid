/*
 * Copyright (C) 2016 Aleksei Skoriatin
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

import android.view.View;

import net.xkor.genaroid.annotations.ViewById_Exp;
import net.xkor.genaroid.internal.Bindable;

class ViewByIdTemplate<$fieldClass$ extends View> implements Bindable {
    $fieldClass$ $field$;
    ViewById_Exp $annotation$;

    @Override
    public void _gen_bind(View rootView) {
        $methodBody$();
        $field$ = ($fieldClass$) rootView.findViewById($annotation$.value());
    }

    @Override
    public void _gen_unbind() {
        $methodBody$();
        $field$ = null;
    }
}
