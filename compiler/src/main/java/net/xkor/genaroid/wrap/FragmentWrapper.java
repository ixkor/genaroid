package net.xkor.genaroid.wrap;

import com.sun.tools.javac.model.JavacElements;

public class FragmentWrapper extends BaseFragmentWrapper {
    protected FragmentWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public FragmentWrapper(JavacElements utils) {
        super(utils, "android.app.Fragment");
    }
}
