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

    public MethodSymbol getOnRestartMethod() {
        return (MethodSymbol) getMember("onRestart");
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
