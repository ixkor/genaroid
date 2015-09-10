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

package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.model.JavacElements;

public abstract class BaseFragmentWrapper extends BaseUiContainerWrapper {
    protected BaseFragmentWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public MethodSymbol getOnAttachMethod() {
        return (MethodSymbol) getMember("onAttach");
    }

    public MethodSymbol getOnCreateViewMethod() {
        return (MethodSymbol) getMember("onCreateView");
    }

    public MethodSymbol getOnViewCreatedMethod() {
        return (MethodSymbol) getMember("onViewCreated");
    }

    public MethodSymbol getOnActivityCreatedMethod() {
        return (MethodSymbol) getMember("onActivityCreated");
    }

    public MethodSymbol getOnRestartMethod() {
        return (MethodSymbol) getMember("onRestart");
    }

    public MethodSymbol getOnDestroyViewMethod() {
        return (MethodSymbol) getMember("onDestroyView");
    }

    public MethodSymbol getOnDetachMethod() {
        return (MethodSymbol) getMember("onDetach");
    }
}
