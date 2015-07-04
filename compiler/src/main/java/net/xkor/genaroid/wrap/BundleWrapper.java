package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Filter;

import net.xkor.genaroid.GenaroidEnvironment;

import javax.lang.model.element.Modifier;

public class BundleWrapper extends BaseClassWrapper {
    private final Types types;
    private final Type parcelableType;
    private final Type arrayListType;

    public BundleWrapper(GenaroidEnvironment environment) {
        super(environment.getUtils(), "android.os.Bundle");
        types = environment.getTypes();
        parcelableType = environment.getUtils().getTypeElement("android.os.Parcelable").asType();
        arrayListType = environment.getUtils().getTypeElement("java.util.ArrayList").asType();
    }

    public MethodSymbol getMethodForPutType(Type fieldType) {
        MethodSymbol methodSymbol = null;
        Symbol.ClassSymbol classSymbol = getClassSymbol();
        do {
            for (Symbol symbol : classSymbol.members().getElements(new PutFilter(fieldType))) {
                if (methodSymbol == null) {
                    methodSymbol = (MethodSymbol) symbol;
                } else {
                    Type secondParamType = symbol.asType().getParameterTypes().get(1);
                    if (types.isSameType(fieldType, secondParamType)) {
                        methodSymbol = (MethodSymbol) symbol;
                    }
                }
            }
            classSymbol = (Symbol.ClassSymbol) classSymbol.getSuperclass().asElement();
        } while (classSymbol != null);
        return methodSymbol;
    }

    public MethodSymbol getMethodForGetType(Type fieldType) {
        MethodSymbol methodSymbol = null;
        Symbol.ClassSymbol classSymbol = getClassSymbol();
        do {
            for (Symbol symbol : classSymbol.members().getElements(new GetFilter(fieldType))) {
                if (methodSymbol == null) {
                    methodSymbol = (MethodSymbol) symbol;
                } else {
                    Type secondParamType = symbol.asType().getReturnType();
                    if (types.isSameType(fieldType, secondParamType)) {
                        methodSymbol = (MethodSymbol) symbol;
                    }
                }
            }
            classSymbol = (Symbol.ClassSymbol) classSymbol.getSuperclass().asElement();
        } while (classSymbol != null);
        return methodSymbol;
    }

    private class GetFilter implements Filter<Symbol> {
        private Type fieldType;

        public GetFilter(Type fieldType) {
            this.fieldType = fieldType;
        }

        @Override
        public boolean accepts(Symbol symbol) {
            if (symbol instanceof MethodSymbol) {
                if (symbol.asType().getParameterTypes().size() != 1) {
                    return false;
                }
                if (!symbol.getModifiers().contains(Modifier.PUBLIC)) {
                    return false;
                }
                if (!symbol.getSimpleName().toString().startsWith("get")) {
                    return false;
                }
                Type returnType = symbol.asType().getReturnType();
                if (fieldType.isPrimitive()) {
                    return types.isSameType(fieldType, returnType);
                }
                if (types.isArray(fieldType)) {
                    if (!types.isArray(returnType)) {
                        return false;
                    }
                    return types.isSameType(types.elemtype(fieldType), types.elemtype(returnType));
                }
                if (types.isSubtype(fieldType, arrayListType)) {

                }
                return types.isSubtype(fieldType, returnType);
            }
            return false;
        }
    }

    private class PutFilter implements Filter<Symbol> {
        private Type fieldType;

        public PutFilter(Type fieldType) {
            this.fieldType = fieldType;
        }

        @Override
        public boolean accepts(Symbol symbol) {
            if (symbol instanceof MethodSymbol) {
                if (symbol.asType().getParameterTypes().size() != 2) {
                    return false;
                }
                if (!symbol.getModifiers().contains(Modifier.PUBLIC)) {
                    return false;
                }
                if (!symbol.getSimpleName().toString().startsWith("put")) {
                    return false;
                }
                Type secondParamType = symbol.asType().getParameterTypes().get(1);
                if (fieldType.isPrimitive()) {
                    return types.isSameType(fieldType, secondParamType);
                }
                if (types.isArray(fieldType)) {
                    if (!types.isArray(secondParamType)) {
                        return false;
                    }
                    return types.isSameType(types.elemtype(fieldType), types.elemtype(secondParamType));
                }
                return types.isSubtype(fieldType, secondParamType);
            }
            return false;
        }
    }
}
