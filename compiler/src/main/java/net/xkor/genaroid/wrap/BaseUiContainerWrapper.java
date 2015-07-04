package net.xkor.genaroid.wrap;

import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.model.JavacElements;

public abstract class BaseUiContainerWrapper extends BaseClassWrapper {
    protected BaseUiContainerWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public MethodSymbol getOnCreateMethod() {
        return (MethodSymbol) getMember("onCreate");
    }

    public MethodSymbol getOnStartMethod() {
        return (MethodSymbol) getMember("onStart");
    }

    public MethodSymbol getOnResumeMethod() {
        return (MethodSymbol) getMember("onResume");
    }

    public MethodSymbol getOnSaveInstanceStateMethod() {
        return (MethodSymbol) getMember("onSaveInstanceState");
    }

    public MethodSymbol getOnPauseMethod() {
        return (MethodSymbol) getMember("onPause");
    }

    public MethodSymbol getOnStopMethod() {
        return (MethodSymbol) getMember("onStop");
    }

    public MethodSymbol getOnDestroyMethod() {
        return (MethodSymbol) getMember("onDestroy");
    }
}
