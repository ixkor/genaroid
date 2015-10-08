Genaroid
=======
Fast Android development with annotation processing. Field binding for Android views and easy field state restoring which uses annotation processing to generate boilerplate code for you.
 * Eliminate `findViewById` calls by using `@ViewById` on fields.
 * Eliminate `bundle.get*` and `bundle.put*` calls by using `@InstanceState` on fields.
 * Eliminate `setContentView` and `onCreateView` calls and create builders for Fragments and Activities by using `@GActivity` and `@GFragment` on classes and `@BuilderParam` on field that should be set by builder.

```java
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
```

Usage
--------

Gradle:
```groovy
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.7'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'net.xkor.genaroid:core:1.1.2'
    apt 'net.xkor.genaroid:compiler:1.1.2'
}
```

License
-------

    Copyright 2013 Aleksei Skoriatin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
