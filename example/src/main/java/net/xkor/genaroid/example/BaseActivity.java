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

import android.accounts.Account;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.xkor.genaroid.annotations.BuilderParam;
import net.xkor.genaroid.annotations.GActivity;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

import java.io.Serializable;
import java.util.ArrayList;

@GActivity(R.layout.activity_main)
public class BaseActivity extends AppCompatActivity {

    @ViewById(R.id.testId)
    private View test;

    @ViewById(R.id.testId)
    private View test5;

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

    @BuilderParam(value = "testKey", optional = true)
    private int testArg;

    @BuilderParam("testKey2")
    private int testArg2;

    @BuilderParam()
    private int testArg4;

    private static class TestClass implements Serializable {
    }
}
