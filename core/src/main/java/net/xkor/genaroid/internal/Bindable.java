package net.xkor.genaroid.internal;

import android.view.View;

/**
 * For internal usage only!
 */
public interface Bindable {
    void _gen_bind(View rootView);

    void _gen_unbind();
}
