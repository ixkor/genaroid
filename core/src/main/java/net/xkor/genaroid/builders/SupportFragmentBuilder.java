package net.xkor.genaroid.builders;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SupportFragmentBuilder<F extends Fragment, B extends SupportFragmentBuilder<F, B>> extends BaseFragmentBuilder<F, B> {
    protected SupportFragmentBuilder(Class<F> fragmentClass, Class<B> builderClass) {
        super(fragmentClass, builderClass);
    }

    @Override
    protected void setArgs(F fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }
}
