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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;

import net.xkor.genaroid.GenaroidEnvironment;

import java.util.HashSet;

public class BundleWrapper extends BaseClassWrapper {
    private final Types types;
    private final HashSet<Type> classes = new HashSet<>();
    private final Type parcelableType;
    private final Type serializableType;
    private final Type arrayListType;
    private final Type sparseArrayType;

    public BundleWrapper(GenaroidEnvironment environment) {
        super(environment.getUtils(), "android.os.Bundle");
        types = environment.getTypes();

        addType("java.lang.String");
        addType("java.lang.Integer");
        addType("java.lang.CharSequence");
        addType("android.os.Bundle");
        addType("android.util.Size");
        addType("android.util.SizeF");

        parcelableType = getType("android.os.Parcelable");
        serializableType = getType("java.io.Serializable");
        arrayListType = getType("java.util.ArrayList");
        sparseArrayType = getType("android.util.SparseArray");
    }

    private void addType(String typeName) {
        classes.add(getType(typeName));
    }

    private Type getType(String typeName) {
        return getUtils().getTypeElement(typeName).asType();
    }

    public MethodSymbol getMethodForType(Type fieldType, boolean isGetter) {
        String prefix = isGetter ? "get" : "put";
        if (types.isArray(fieldType)) {
            return getMethod(types.elemtype(fieldType), prefix, "Array");
        } else if (types.isSameType(fieldType.asElement().asType(), arrayListType)) {
            return getMethod(fieldType.getTypeArguments().get(0), prefix, "ArrayList");
        } else if (types.isSameType(fieldType.asElement().asType(), sparseArrayType)) {
            Type typeArg = fieldType.getTypeArguments().get(0);
            if (types.isSubtype(typeArg, parcelableType)) {
                return getMethodRecursive(prefix + "SparseParcelableArray");
            }
        } else {
            return getMethod(fieldType, prefix, "");
        }
        return null;
    }

    private Type findBundleType(Type type) {
        if (type.isPrimitive()) {
            return type;
        } else {
            if (classes.contains(type)) {
                return type;
            } else if (types.isSubtype(type, parcelableType)) {
                return parcelableType;
            } else if (types.isSubtype(type, serializableType)) {
                return serializableType;
            }
        }
        return null;
    }

    public MethodSymbol getMethod(Type type, String prefix, String suffix) {
        type = findBundleType(type);
        if (type == null) {
            return null;
        }

        String typeName = type.asElement().getSimpleName().toString();
        String mainNamePart;
        if (type.isPrimitive()) {
            mainNamePart = Character.toString(typeName.charAt(0)).toUpperCase() + typeName.substring(1);
        } else {
            mainNamePart = typeName;
        }
        return getMethodRecursive(prefix + mainNamePart + suffix);
    }
}
