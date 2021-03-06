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

package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;

public class InflatableWrapper extends BaseClassWrapper {
    public InflatableWrapper(JavacElements utils) {
        super(utils, "net.xkor.genaroid.internal.Inflatable");
    }

    public Symbol.MethodSymbol getGetLayoutIdMethod() {
        return (Symbol.MethodSymbol) getMember("_gen_getLayoutId");
    }
}
