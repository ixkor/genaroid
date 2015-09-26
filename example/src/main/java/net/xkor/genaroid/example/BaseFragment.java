package net.xkor.genaroid.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.xkor.genaroid.annotations.BuilderParam;
import net.xkor.genaroid.annotations.GBaseFragment;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

import java.util.ArrayList;

@GBaseFragment
public class BaseFragment extends Fragment {
    @ViewById(R.id.testId)
    private View test;

    @ViewById(R.id.testId)
    private TextView test2;

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

    @BuilderParam(value = "testKey", optional = true)
    private int testArg;

    @BuilderParam("testKey2")
    private int testArg2;

    @BuilderParam()
    private int testArg3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new BaseActivityBuilder(getActivity(), 1, 1).testArg(2).start();
        new BaseFragmentBuilder(1, 2).testArg(6).instantiate();
        return inflater.inflate(R.layout.activity_main, container, false);
    }
}
