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
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.ViewWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

public class ViewByIdProcessor implements SubProcessor {
    private static final String VIEW_BY_ID_ANNOTATION = "net.xkor.genaroid.annotations.ViewById";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol viewByIdType = utils.getTypeElement(VIEW_BY_ID_ANNOTATION);
//        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
//        BaseFragmentWrapper fragmentWrapper = new FragmentWrapper(utils);
//        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        ViewWrapper viewWrapper = new ViewWrapper(utils);
        ExecutorWrapper executorWrapper = new ExecutorWrapper(utils);

        Set<GField> allFields = environment.getGElementsAnnotatedWith(viewByIdType, GField.class);
        for (GField field : allFields) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(viewByIdType);
            JCTree fieldType = field.getTree().getType();
            JCExpression value = annotation.getArguments().get(0);
            if (value instanceof JCAssign) {
                value = ((JCAssign) value).rhs;
            }
            if (!environment.getTypes().isSubtype(((Symbol.VarSymbol) field.getElement()).asType(), viewWrapper.getClassSymbol().asType())) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + viewByIdType.getSimpleName() + " can be applied only to field with type extended of View",
                        field.getElement());
            }

            boolean executorImplemented = field.getGClass().isSubClass(executorWrapper.getClassSymbol());
            if (!executorImplemented) {
                GClass classToExecutorImplement = field.getGClass();
                for (GField otherField : allFields) {
                    if (otherField.getGClass() != classToExecutorImplement && classToExecutorImplement.isSubClass(otherField.getGClass())) {
                        classToExecutorImplement = otherField.getGClass();
                    }
                }
                classToExecutorImplement.implement(executorWrapper.getClassSymbol());
            }

            GMethod onViewCreatedMethod = field.getGClass().overrideMethod(executorWrapper.getFindViewsMethod(), true);
            GMethod onDestroyViewMethod = field.getGClass().overrideMethod(executorWrapper.getClearViewsMethod(), true);
            Name viewParam = onViewCreatedMethod.getParamName(0);
            String fieldSetCode = String.format("this.%s = (%s) %s.findViewById(%s);",
                    field.getName(), fieldType, viewParam, value);
            String fieldUnsetCode = String.format("this.%s = null;", field.getName());
            JCStatement fieldSetStatement = environment.createParser(fieldSetCode).parseStatement();
            JCStatement fieldUnsetStatement = environment.createParser(fieldUnsetCode).parseStatement();

            onViewCreatedMethod.prependCode(fieldSetStatement);
            onDestroyViewMethod.prependCode(fieldUnsetStatement);

//            if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
//                GMethod onContentChangedMethod = field.getGClass().overrideMethod(activityWrapper.getOnContentChangedMethod(), true);
//                String fieldSetCode = String.format("this.%s = (%s) findViewById(%s);",
//                        field.getName(), fieldType, value);
//                JCStatement fieldSetStatement = environment.createParser(fieldSetCode).parseStatement();
//
//                onContentChangedMethod.prependCode(fieldSetStatement);
//            } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())
//                    || field.getGClass().isSubClass(fragmentWrapper.getClassSymbol())) {
//                GMethod onViewCreatedMethod = field.getGClass().overrideMethod(fragmentWrapper.getOnViewCreatedMethod(), true);
//                GMethod onDestroyViewMethod = field.getGClass().overrideMethod(fragmentWrapper.getOnDestroyViewMethod(), true);
//                Name viewParam = onViewCreatedMethod.getParamName(0);
//                String fieldSetCode = String.format("this.%s = (%s) %s.findViewById(%s);",
//                        field.getName(), fieldType, viewParam, value);
//                String fieldUnsetCode = String.format("this.%s = null;", field.getName());
//                JCStatement fieldSetStatement = environment.createParser(fieldSetCode).parseStatement();
//                JCStatement fieldUnsetStatement = environment.createParser(fieldUnsetCode).parseStatement();
//
//                onViewCreatedMethod.prependCode(fieldSetStatement);
//                onDestroyViewMethod.prependCode(fieldUnsetStatement);
//            } else {
//                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                        "Annotation " + viewByIdType.getSimpleName() + " can be applied only to field of Activity or Fragment",
//                        field.getElement());
//            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(VIEW_BY_ID_ANNOTATION);
    }

    private class ExecutorWrapper extends BaseClassWrapper {
        public ExecutorWrapper(JavacElements utils) {
            super(utils, VIEW_BY_ID_ANNOTATION + ".Executor");
        }

        public Symbol.MethodSymbol getFindViewsMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_findViews");
        }

        public Symbol.MethodSymbol getClearViewsMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_clearViews");
        }
    }
}
