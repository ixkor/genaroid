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

package net.xkor.genaroid.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = true, value = {InjectGenaroidCall.NONE, InjectGenaroidCall.BIND, InjectGenaroidCall.INSTANCE_STATE, InjectGenaroidCall.ALL})
@Retention(RetentionPolicy.SOURCE)
public @interface InjectGenaroidCall {
    int NONE = 0;
    int BIND = 1;
    int INSTANCE_STATE = 2;
    int ALL = BIND | INSTANCE_STATE;
}
