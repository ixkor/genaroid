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
