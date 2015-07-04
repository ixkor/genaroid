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

import java.util.ArrayList;
import java.util.List;

public class GenaroidUiDelegate {
    private List<GenaroidUiClassHandler> handlers = new ArrayList<>();
    private Owner owner;

    public GenaroidUiDelegate(Owner owner) {
        this.owner = owner;
    }

    public void addHandler(GenaroidUiClassHandler handler) {
        handlers.add(handler);
    }

    public void onCreate(Bundle savedInstanceState) {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onCreate(savedInstanceState);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View view = null;
        for (GenaroidUiClassHandler handler : handlers) {
            view = handler.onCreateView(inflater, container, view);
        }
        return view;
    }

    public void onViewCreated(View view) {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onViewCreated(view);
        }
    }

    public void onStart() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onStart();
        }
    }

    public void onResume() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onResume();
        }
    }

    public void onPause() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onPause();
        }
    }

    public void onStop() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onStop();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onSaveInstanceState(outState);
        }
    }

    public void onDestroyView() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onDestroyView();
        }
    }

    public void onDestroy() {
        for (GenaroidUiClassHandler handler : handlers) {
            handler.onDestroy();
        }
    }

    public interface Owner {
        GenaroidUiDelegate getGenaroidDelegate();
    }
}
