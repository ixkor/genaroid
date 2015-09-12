/**
 * Copyright (C) 2015 Aleksei Skoriatin
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xkor.genaroid.example;

import android.accounts.Account;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xkor.genaroid.annotations.BaseActivity;
import net.xkor.genaroid.annotations.BaseFragment;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

import java.io.Serializable;
import java.util.ArrayList;

@BaseActivity
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.testId)
    private View test;

    @ViewById(R.id.testId)
    private View test2;

    @InstanceState
    private int intField;

    @InstanceState
    private String stringField;

    @InstanceState
    private int[] intArrayField;

    @InstanceState
    private float floatField;

    @InstanceState
    private Bundle bundleField;

    @InstanceState
    private TestClass serField;

    @InstanceState
    private Account accountField;
    @InstanceState
    private Account[] accountsField;
    @InstanceState
    private ArrayList<Account> accountListField;

    @InstanceState
    private ArrayList<String> stringArrayField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @BaseFragment
    public static class TestFragment extends Fragment {

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

    public static class TestClass implements Serializable {
        private int qwerty;
    }
}
