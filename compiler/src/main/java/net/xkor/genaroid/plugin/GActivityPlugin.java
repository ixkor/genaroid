/*
 * Copyright (C) 2016 Aleksei Skoriatin
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

package net.xkor.genaroid.plugin;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.GActivity;
import net.xkor.genaroid.annotations.InjectGenaroidCall;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;
import net.xkor.genaroid.wrap.InflatableWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
public class GActivityPlugin extends GenaroidPlugin {
    private static final String ANNOTATION_CLASS_NAME = GActivity.class.getCanonicalName();

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
        InflatableWrapper inflatableWrapper = new InflatableWrapper(utils);

        Set<GClass> activities = environment.getGElementsAnnotatedWith(GActivity.class, GClass.class);
        List<GClass> sortedActivities = new ArrayList<>(activities);
        Collections.sort(sortedActivities, GClass.HIERARCHY_LEVEL_COMPARATOR);
        environment.setActivities(sortedActivities);

        for (GClass activity : activities) {
            JCTree.JCAnnotation jcAnnotation = activity.extractAnnotation(instanceStateType);
            GActivity annotation = activity.getAnnotation(GActivity.class);

            if (!activity.isSubClass(activityWrapper.getClassSymbol())) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + instanceStateType.getSimpleName() + " can be applied only to subclasses of Activity",
                        activity.getElement());
                continue;
            }

            if (annotation.value() != 0) {
                String layoutId = String.valueOf(annotation.value());
                for (JCTree.JCExpression value : jcAnnotation.getArguments()) {
                    if (value instanceof JCTree.JCAssign) {
                        if (((JCTree.JCAssign) value).lhs.toString().equals("value")) {
                            layoutId = ((JCTree.JCAssign) value).rhs.toString();
                        }
                    } else {
                        layoutId = value.toString();
                    }
                }

                activity.implementIfNeeded(inflatableWrapper.getClassSymbol());
                activity.overrideMethod(inflatableWrapper.getGetLayoutIdMethod(), false)
                        .appendCode("return %s;", layoutId);
            }

            if (activity.isBaseWithAnnotation(GActivity.class)) {
                activity.getGUnit().addNewImports(GenaroidEnvironment.GENAROID_MAIN_CLASS);
                GMethod onCreateMethod = activity.overrideMethod(activityWrapper.getOnCreateMethod(), true);

                if ((annotation.injectCalls() & InjectGenaroidCall.INSTANCE_STATE) != 0) {
                    activity.overrideMethod(activityWrapper.getOnSaveInstanceStateMethod(), true)
                            .appendCode("Genaroid.saveInstanceState(this, $p0);");
                    onCreateMethod.prependCode("Genaroid.restoreInstanceState(this, $p0);");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.BIND) != 0) {
                    activity.overrideMethod(activityWrapper.getOnContentChangedMethod(), true)
                            .appendCode("Genaroid.bind(this);");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.READ_PARAMS) != 0) {
                    onCreateMethod.prependCode("Genaroid.readParams(this);");
                    activity.overrideMethod(activityWrapper.getOnNewIntentMethod(), true)
                            .appendCode("Genaroid.readParams(this, $p0.getExtras());");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.INFLATE_LAYOUT) != 0) {
                    onCreateMethod.appendCodeAfterSuper("Genaroid.setContentView(this);");
                }
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }
}
