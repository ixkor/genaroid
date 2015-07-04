package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Filter;

import java.util.HashMap;
import java.util.Iterator;

public class BaseClassWrapper {
    private final JavacElements utils;
    private final String classFullName;
    private final Symbol.ClassSymbol classSymbol;
    private final HashMap<String, Symbol> membersCache = new HashMap<>();

    private static final Filter<Symbol> noFilter = new Filter<Symbol>() {
        public boolean accepts(Symbol var1) {
            return true;
        }
    };

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
}
