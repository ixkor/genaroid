/*
 * Copyright (C) 2015 Aleksei Skoriatin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xkor.genaroid.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import net.xkor.genaroid.annotations.BuilderParam;
import net.xkor.genaroid.annotations.GFragment;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

import java.util.ArrayList;

@GFragment(R.layout.activity_main)
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
}
