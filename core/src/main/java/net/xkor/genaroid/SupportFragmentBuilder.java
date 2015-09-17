package net.xkor.genaroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SupportFragmentBuilder<T extends Fragment> extends BaseFragmentBuilder<T> {
    public SupportFragmentBuilder(Class<T> fragmentClass) {
        super(fragmentClass);
    }

    @Override
    protected void setArgs(T fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }
}
