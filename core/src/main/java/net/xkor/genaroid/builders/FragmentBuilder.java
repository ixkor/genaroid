package net.xkor.genaroid.builders;

import android.app.Fragment;
import android.os.Bundle;

public class FragmentBuilder<F extends Fragment, B extends FragmentBuilder<F, B>> extends BaseFragmentBuilder<F, B> {
    protected FragmentBuilder(Class<F> fragmentClass, Class<B> builderClass) {
        super(fragmentClass, builderClass);
    }

    @Override
    protected void setArgs(F fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }
}
