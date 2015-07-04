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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.ViewById;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;
import net.xkor.genaroid.wrap.BaseFragmentWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;
import net.xkor.genaroid.wrap.ViewWrapper;

import javax.tools.Diagnostic;

public class ViewByIdProcessor implements SubProcessor {

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
        BaseFragmentWrapper fragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        ViewWrapper viewWrapper = new ViewWrapper(utils);

        for (GField field : environment.getGElementsAnnotatedWith(ViewById.class, GField.class)) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(ViewById.class);
            JCTree fieldType = field.getTree().getType();
            JCExpression value = annotation.getArguments().get(0);
            if (value instanceof JCAssign) {
                value = ((JCAssign) value).rhs;
            }
            if (!environment.getTypes().isSubtype(((Symbol.VarSymbol) field.getElement()).asType(), viewWrapper.getClassSymbol().asType())) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + ViewById.class.getSimpleName() + " can be applied only to field with type extended of View",
                        field.getElement());
            }
            if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
                GMethod onContentChangedMethod = field.getGClass().overrideMethod(activityWrapper.getOnContentChangedMethod(), true);
                String fieldSetCode = String.format("this.%s = (%s) findViewById(%s);",
                        field.getName(), fieldType, value);
                JCStatement fieldSetStatement = environment.createParser(fieldSetCode).parseStatement();

                onContentChangedMethod.prependCode(fieldSetStatement);
            } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())
                    || field.getGClass().isSubClass(fragmentWrapper.getClassSymbol())) {
                GMethod onViewCreatedMethod = field.getGClass().overrideMethod(fragmentWrapper.getOnViewCreatedMethod(), true);
                GMethod onDestroyViewMethod = field.getGClass().overrideMethod(fragmentWrapper.getOnDestroyViewMethod(), true);
                Name viewParam = onViewCreatedMethod.getParamName(0);
                String fieldSetCode = String.format("this.%s = (%s) %s.findViewById(%s);",
                        field.getName(), fieldType, viewParam, value);
                String fieldUnsetCode = String.format("this.%s = null;", field.getName());
                JCStatement fieldSetStatement = environment.createParser(fieldSetCode).parseStatement();
                JCStatement fieldUnsetStatement = environment.createParser(fieldUnsetCode).parseStatement();

                onViewCreatedMethod.prependCode(fieldSetStatement);
                onDestroyViewMethod.prependCode(fieldUnsetStatement);
            } else {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + ViewById.class.getSimpleName() + " can be applied only to field of Activity or Fragment",
                        field.getElement());
            }
        }
    }
}
