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

package net.xkor.genaroid.processing;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

public class BaseActivityProcessor implements SubProcessor {
    private static final String ANNOTATION_CLASS_NAME = "net.xkor.genaroid.annotations.GBaseActivity";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);

        Set<GClass> activities = environment.getGElementsAnnotatedWith(instanceStateType, GClass.class);
        for (GClass activity : activities) {
            activity.extractAnnotation(instanceStateType);

            if (!activity.isSubClass(activityWrapper.getClassSymbol())) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + instanceStateType.getSimpleName() + " can be applied only to subclasses of Activity",
                        activity.getElement());
                continue;
            }

            activity.getGUnit().addNewImports(GenaroidEnvironment.GENAROID_MAIN_CLASS);

            GMethod onSaveInstanceStateMethod = activity.overrideMethod(activityWrapper.getOnSaveInstanceStateMethod(), true);
            Name bundleParam = onSaveInstanceStateMethod.getParamName(0);
            String code = String.format("Genaroid.saveInstanceState(this, %s);", bundleParam);
            JCStatement statement = environment.createParser(code).parseStatement();
            onSaveInstanceStateMethod.appendCode(statement);

            GMethod onCreateMethod = activity.overrideMethod(activityWrapper.getOnCreateMethod(), true);
            bundleParam = onCreateMethod.getParamName(0);
            code = String.format("Genaroid.restoreInstanceState(this, %s);", bundleParam);
            statement = environment.createParser(code).parseStatement();
            onCreateMethod.prependCode(statement);
            code = "Genaroid.readParams(this);";
            statement = environment.createParser(code).parseStatement();
            onCreateMethod.prependCode(statement);

            GMethod onContentChangedMethod = activity.overrideMethod(activityWrapper.getOnContentChangedMethod(), true);
            code = "Genaroid.bind(this);";
            statement = environment.createParser(code).parseStatement();
            onContentChangedMethod.appendCode(statement);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }
}
