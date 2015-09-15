package net.xkor.genaroid.internal;

import android.os.Bundle;

/**
 * For internal usage only!
 */
public interface Restorable {
    void _gen_saveInstanceState(Bundle outState);

    void _gen_restoreInstanceState(Bundle savedState);
}
