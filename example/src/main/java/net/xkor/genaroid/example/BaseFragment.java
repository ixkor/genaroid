package net.xkor.genaroid.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xkor.genaroid.annotations.GBaseFragment;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

import java.util.ArrayList;

@GBaseFragment
public class BaseFragment extends Fragment {
    @ViewById(R.id.testId)
    private View test;

    @ViewById(R.id.testId)
    private View test2;

    @InstanceState
    private String stringField;

    @InstanceState
    private int[] intArrayField;

    @InstanceState
    private float floatField;

    @InstanceState
    private Bundle bundleField;

    @InstanceState
    private ArrayList<String> stringArrayField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }
}
