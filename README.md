Genaroid
=======
Fast Android development with annotation processing. Field binding for Android views and easy field state restoring which uses annotation processing to generate boilerplate code for you.
 * Eliminate `findViewById` calls by using `@ViewById` on fields.
 * Eliminate `bundle.get*` and `bundle.put*` calls by using `@InstanceState` on fields.

```java
@GBaseActivity
public class BaseActivity extends Activity {
    @ViewById(R.id.progress)
    private ProgressBar progress;
    @ViewById(R.id.content)
    private ViewGroup content;

    @InstanceState
    private boolean loaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        // TODO Use fields...
    }
}
```

Usage
--------

Maven:
```xml
<dependency>
  <groupId>net.xkor.genaroid</groupId>
  <artifactId>core</artifactId>
  <version>1.0.1</version>
</dependency>
<dependency>
  <groupId>net.xkor.genaroid</groupId>
  <artifactId>compiler</artifactId>
  <version>1.0.1</version>
  <optional>true</optional>
</dependency>
```
or Gradle:
```groovy
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.7'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'net.xkor.genaroid:core:1.0.1'
    apt 'net.xkor.genaroid:compiler:1.0.1'
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
