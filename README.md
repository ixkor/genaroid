![Image](https://github.com/ixkor/genaroid/blob/master/github_banner.png)

**Genaroid** is an Open Source library that **speeds up** Android development.
It takes care of the **plumbing**, and lets you concentrate on what's really important. By **simplifying** your code, it facilitates its **maintenance**.

[ ![Download](https://api.bintray.com/packages/ixkor/maven/genaroid:compiler/images/download.svg) ](https://bintray.com/ixkor/maven/genaroid:compiler/_latestVersion)

Example:
```java
@GFragment(R.layout.login_fragment)
public class LoginFragment extends BaseFragment {
    @BuilderParam
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
    }

    @OnClick(R.id.sign_in)
    private void signInClick() {
        if (TextUtils.isEmpty(loginField.getText())) {
            authError = "Login is empty";
            loginField.setError(authError);
        }
        // do auth...
    }
}

// instantiate fragment:
LoginFragment fragment = new LoginFragmentBuilder("last login").instantiate();
```

## Configuration
Gradle:
```groovy
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'net.xkor.genaroid:core:1.3.0'
    apt 'net.xkor.genaroid:compiler:1.3.0'
}
```

## Available Annotations:
* [@ViewById](https://github.com/ixkor/genaroid/wiki#viewbyid) - inject view to Activity, Fragment or other object field;
* [@InstanceState](https://github.com/ixkor/genaroid/wiki#instancestate) - save and restore a field value when Activity or Fragment are recreated;
* [@BuilderParam](https://github.com/ixkor/genaroid/wiki#builderparam) - add a field to Activity or Fragment builder;
* [@GActivity and @GFragment](https://github.com/ixkor/genaroid/wiki#gactivity-and-gfragment);
* [@OnClick and other events](https://github.com/ixkor/genaroid/wiki#events);
* [@CustomListener](https://github.com/ixkor/genaroid/wiki#customlistener) - user defined events;

[Full documentation](https://github.com/ixkor/genaroid/wiki)

## License
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
