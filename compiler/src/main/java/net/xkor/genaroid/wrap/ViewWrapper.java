package net.xkor.genaroid.wrap;

import com.sun.tools.javac.model.JavacElements;

public class ViewWrapper extends BaseClassWrapper {
    protected ViewWrapper(JavacElements utils, String classFullName) {
        super(utils, classFullName);
    }

    public ViewWrapper(JavacElements utils) {
        super(utils, "android.view.View");
    }
}
