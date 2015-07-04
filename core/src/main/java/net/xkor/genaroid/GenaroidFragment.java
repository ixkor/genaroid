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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GenaroidFragment extends Fragment implements GenaroidUiDelegate.Owner {
    private GenaroidUiDelegate genaroidDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGenaroidDelegate().onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getGenaroidDelegate().onCreateView(inflater, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getGenaroidDelegate().onViewCreated(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        getGenaroidDelegate().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getGenaroidDelegate().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getGenaroidDelegate().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getGenaroidDelegate().onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getGenaroidDelegate().onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getGenaroidDelegate().onDestroyView();
    }

    @Override
    public void onDestroy() {
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
