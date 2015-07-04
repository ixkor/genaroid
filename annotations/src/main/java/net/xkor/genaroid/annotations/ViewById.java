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
package net.xkor.genaroid.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Use it on {@link android.view.View} or {@link android.view.View} subtype
 * fields in a view related class.
 * </p>
 * <p>
 * The annotation value should be one of R.id.* fields.
 * </p>
 * <blockquote>
 *
 * Example :
 *
 * <pre>
 * public class MyActivity extends GenaroidActivity {
 *
 * 	&#064;ViewById(R.id.myTextView)
 * 	TextView textView;
 *
 * 	&#064;Override
 * 	protected void onCreate(Bundle savedInstanceState) {
 * 		super.onCreate(savedInstanceState);
 * 		setContentView(R.layout.my_activity);
 * 		myEditText.setText(&quot;Date: &quot; + new Date());
 *    }
 * }
 * </pre>
 *
 * </blockquote>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface ViewById {
    /**
     * The R.id.* field which refers to the injected View.
     *
     * @return the id of the View
     */
    int value();
}
