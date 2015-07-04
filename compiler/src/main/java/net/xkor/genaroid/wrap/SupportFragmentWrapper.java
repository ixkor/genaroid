package net.xkor.genaroid.wrap;

import com.sun.tools.javac.model.JavacElements;

public class SupportFragmentWrapper extends BaseFragmentWrapper {
    protected SupportFragmentWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public SupportFragmentWrapper(JavacElements utils) {
        super(utils, "android.support.v4.app.Fragment");
    }
}
