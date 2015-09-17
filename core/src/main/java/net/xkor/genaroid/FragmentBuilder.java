package net.xkor.genaroid;

import android.app.Fragment;
import android.os.Bundle;

public class FragmentBuilder<T extends Fragment> extends BaseFragmentBuilder<T> {
    public FragmentBuilder(Class<T> fragmentClass) {
        super(fragmentClass);
    }

    @Override
    protected void setArgs(T fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }
}
