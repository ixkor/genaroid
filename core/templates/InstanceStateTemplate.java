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

import net.xkor.genaroid.internal.Restorable;

class InstanceStateParcelableArrayTemplate implements Restorable {
    private static final String KEY_$FIELD_NAME$ = "key_$FIELD_NAME$";

    android.os.Parcelable[] $field$;

    @Override
    public void _gen_saveInstanceState(android.os.Bundle outState) {
        $methodBody$();
        param0.$betterBundlePutter$(KEY_$FIELD_NAME$, $field$);
    }

    @Override
    public void _gen_restoreInstanceState(android.os.Bundle savedState) {
        $methodBody$();
        $field$ = net.xkor.genaroid.Utils.castParcelableArray($fieldType$.class, savedState.getParcelableArray(KEY_$FIELD_NAME$));
    }
}

class InstanceStateTemplate implements Restorable {
    private static final String KEY_$FIELD_NAME$ = "key_$FIELD_NAME$";

    @Override
    public void _gen_saveInstanceState(android.os.Bundle outState) {
        $methodBody$();
        param0.$betterBundlePutter$(KEY_$FIELD_NAME$, $field$);
    }

    @Override
    public void _gen_restoreInstanceState(android.os.Bundle savedState) {
        $methodBody$();
        $field$ = ($fieldType_ifCastNeeded$) savedState.$betterBundleGetter$(KEY_$FIELD_NAME$);
    }
}
