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

package net.xkor.genaroid.processing;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseFragmentWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

public class BaseFragmentProcessor implements SubProcessor {
    private static final String ANNOTATION_CLASS_NAME = "net.xkor.genaroid.annotations.GBaseFragment";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        BaseFragmentWrapper nativeFragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);

        Set<GClass> fragments = environment.getGElementsAnnotatedWith(instanceStateType, GClass.class);
        for (GClass fragment : fragments) {
            fragment.extractAnnotation(instanceStateType);

            BaseFragmentWrapper fragmentWrapper;
            if (fragment.isSubClass(supportFragmentWrapper.getClassSymbol())) {
                fragmentWrapper = supportFragmentWrapper;
            } else if (fragment.isSubClass(nativeFragmentWrapper.getClassSymbol())) {
                fragmentWrapper = nativeFragmentWrapper;
            } else {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + instanceStateType.getSimpleName() + " can be applied only to subclasses of Fragment",
                        fragment.getElement());
                continue;
            }

            fragment.getGUnit().addNewImports(GenaroidEnvironment.GENAROID_MAIN_CLASS);

            GMethod onSaveInstanceStateMethod = fragment.overrideMethod(fragmentWrapper.getOnSaveInstanceStateMethod(), true);
            Name bundleParam = onSaveInstanceStateMethod.getParamName(0);
            String code = String.format("Genaroid.saveInstanceState(this, %s);", bundleParam);
            JCStatement statement = environment.createParser(code).parseStatement();
            onSaveInstanceStateMethod.appendCode(statement);

            GMethod onCreateMethod = fragment.overrideMethod(fragmentWrapper.getOnCreateMethod(), true);
            bundleParam = onCreateMethod.getParamName(0);
            code = String.format("Genaroid.restoreInstanceState(this, %s);", bundleParam);
            statement = environment.createParser(code).parseStatement();
            onCreateMethod.prependCode(statement);

            GMethod onViewCreatedMethod = fragment.overrideMethod(fragmentWrapper.getOnViewCreatedMethod(), true);
            bundleParam = onViewCreatedMethod.getParamName(0);
            code = String.format("Genaroid.findViews(this, %s);", bundleParam);
            statement = environment.createParser(code).parseStatement();
            onViewCreatedMethod.appendCode(statement);

            GMethod OnDestroyViewMethod = fragment.overrideMethod(fragmentWrapper.getOnDestroyViewMethod(), true);
            code = "Genaroid.clearViews(this);";
            statement = environment.createParser(code).parseStatement();
            OnDestroyViewMethod.appendCode(statement);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }
}
