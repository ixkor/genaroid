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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.xkor.genaroid.annotations.BuilderParam;
import net.xkor.genaroid.annotations.GFragment;
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.annotations.ViewById;

@GFragment(R.layout.login_fragment)
public class LoginFragment extends BaseFragment {
    @BuilderParam()
    private String lastLogin;

    @ViewById(R.id.login)
    private EditText loginField;
    @ViewById(R.id.password)
    private EditText passwordField;
    @ViewById(R.id.sign_in)
    private Button signInButton;

    @InstanceState
    private String authError;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginField.setText(lastLogin);
        loginField.setError(authError);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(loginField.getText())) {
                    authError = "Login is empty";
                    loginField.setError(authError);
                }
                // do auth...
            }
        });
    }
}
