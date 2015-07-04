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
package net.xkor.genaroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

public class GenaroidActivity extends AppCompatActivity implements GenaroidUiDelegate.Owner {
    private GenaroidUiDelegate genaroidDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGenaroidDelegate().onCreate(savedInstanceState);
        View view = getGenaroidDelegate().onCreateView(getLayoutInflater(), (ViewGroup) findViewById(android.R.id.content));
        if (view != null) {
            setContentView(view);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        getGenaroidDelegate().onViewCreated(findViewById(android.R.id.content));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getGenaroidDelegate().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGenaroidDelegate().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getGenaroidDelegate().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getGenaroidDelegate().onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getGenaroidDelegate().onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getGenaroidDelegate().onDestroy();
    }

    @Override
    public GenaroidUiDelegate getGenaroidDelegate() {
        if (genaroidDelegate == null) {
            genaroidDelegate = new GenaroidUiDelegate(this);
        }
        return genaroidDelegate;
    }
}
