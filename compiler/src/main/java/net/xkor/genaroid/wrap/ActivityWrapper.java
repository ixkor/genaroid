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

package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Filter;

public class ActivityWrapper extends BaseUiContainerWrapper {
    protected ActivityWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public ActivityWrapper(JavacElements utils) {
        super(utils, "android.app.Activity");
    }

    @Override
    public MethodSymbol getOnCreateMethod() {
        return (MethodSymbol) getMember("onCreate", new Filter<Symbol>() {
            @Override
            public boolean accepts(Symbol symbol) {
                return symbol.asType().getParameterTypes().size() == 1;
            }
        });
    }

    public MethodSymbol getOnContentChangedMethod() {
        return (MethodSymbol) getMember("onContentChanged");
    }

    public MethodSymbol getOnSupportContentChangedMethod() {
        return (MethodSymbol) getMember("onSupportContentChanged");
    }

    public MethodSymbol getOnRestartMethod() {
        return (MethodSymbol) getMember("onRestart");
    }

    public MethodSymbol getOnNewIntentMethod() {
        return (MethodSymbol) getMember("onNewIntent");
    }

    @Override
    public MethodSymbol getOnSaveInstanceStateMethod() {
        return (MethodSymbol) getMember("onSaveInstanceState", new Filter<Symbol>() {
            @Override
            public boolean accepts(Symbol symbol) {
                return symbol.asType().getParameterTypes().size() == 1;
            }
        });
    }
}
