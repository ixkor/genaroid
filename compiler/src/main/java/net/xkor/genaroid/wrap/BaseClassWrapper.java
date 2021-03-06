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
import com.sun.tools.javac.util.Filter;

import java.util.HashMap;
import java.util.Iterator;

public class BaseClassWrapper {
    private static final Filter<Symbol> noFilter = new Filter<Symbol>() {
        public boolean accepts(Symbol var1) {
            return true;
        }
    };
    private final JavacElements utils;
    private final String classFullName;
    private final Symbol.ClassSymbol classSymbol;
    private final HashMap<String, Symbol> membersCache = new HashMap<>();
    private final HashMap<String, Symbol.MethodSymbol> deepMethodsCache = new HashMap<>();

    public BaseClassWrapper(JavacElements utils, String classFullName) {
        this.utils = utils;
        this.classFullName = classFullName;
        classSymbol = utils.getTypeElement(classFullName);
    }

    public String getClassFullName() {
        return classFullName;
    }

    public Symbol.ClassSymbol getClassSymbol() {
        return classSymbol;
    }

    protected JavacElements getUtils() {
        return utils;
    }

    public Symbol getMember(String name) {
        return getMember(name, noFilter);
    }

    public Symbol getMember(String name, Filter<Symbol> filter) {
        Symbol member = membersCache.get(name);
        if (member == null) {
            Iterator<Symbol> iterator = classSymbol.members().getElementsByName(utils.getName(name), filter).iterator();
            if (iterator.hasNext()) {
                member = iterator.next();
                membersCache.put(name, member);
            }
        }
        return member;
    }

    public Symbol.MethodSymbol getMethodRecursive(String name) {
        return getMethodRecursive(name, noFilter);
    }

    public Symbol.MethodSymbol getMethodRecursive(String name, Filter<Symbol> filter) {
        Symbol.MethodSymbol methodSymbol = deepMethodsCache.get(name);
        if (methodSymbol == null) {
            methodSymbol = (Symbol.MethodSymbol) getMember(name, filter);
        }
        Symbol.ClassSymbol classSymbol = getClassSymbol();
        while (classSymbol != null && methodSymbol == null) {
            classSymbol = (Symbol.ClassSymbol) classSymbol.getSuperclass().asElement();
            Iterator<Symbol> iterator = classSymbol.members().getElementsByName(utils.getName(name), filter).iterator();
            if (iterator.hasNext()) {
                methodSymbol = (Symbol.MethodSymbol) iterator.next();
                deepMethodsCache.put(name, methodSymbol);
                return methodSymbol;
            }
        }
        return methodSymbol;
    }
}
