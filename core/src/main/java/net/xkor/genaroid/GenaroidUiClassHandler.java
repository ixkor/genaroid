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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class GenaroidUiClassHandler {

    public GenaroidUiClassHandler(GenaroidUiDelegate delegate) {
        delegate.addHandler(this);
    }

    public void onCreate(Bundle savedInstanceState) {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, View createdBySuper) {
        return createdBySuper;
    }

    public void onViewCreated(View view) {
    }

    public void onStart() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onDestroyView() {
    }

    public void onDestroy() {
    }
}
