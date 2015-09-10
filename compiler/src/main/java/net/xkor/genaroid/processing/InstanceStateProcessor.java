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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

public class InstanceStateProcessor implements SubProcessor {
    private static final String INSTANCE_STATE_ANNOTATION = "net.xkor.genaroid.annotations.InstanceState";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(INSTANCE_STATE_ANNOTATION);
//        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
//        BaseFragmentWrapper fragmentWrapper = new FragmentWrapper(utils);
//        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        BundleWrapper bundleWrapper = new BundleWrapper(environment);
        ExecutorWrapper executorWrapper = new ExecutorWrapper(utils);
        Type serializableType = utils.getTypeElement("java.io.Serializable").asType();

        Set<GField> allFields = environment.getGElementsAnnotatedWith(instanceStateType, GField.class);
        for (GField field : allFields) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(instanceStateType);
            Type fieldType = ((Symbol.VarSymbol) field.getElement()).asType();
            Symbol.MethodSymbol putMethod = bundleWrapper.getMethodForPutType(fieldType);
            Symbol.MethodSymbol getMethod = bundleWrapper.getMethodForGetType(fieldType);
            String fieldNameInBundle = "_gen_" + field.getGClass().getName() + "_" + field.getName();

            if (putMethod == null || getMethod == null) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can't found getter and putter for type of field " + field.getName(),
                        field.getElement());
                continue;
            }

//            BaseUiContainerWrapper uiContainerWrapper;
//            if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
//                uiContainerWrapper = activityWrapper;
//            } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())) {
//                uiContainerWrapper = supportFragmentWrapper;
//            } else if (field.getGClass().isSubClass(fragmentWrapper.getClassSymbol())) {
//                uiContainerWrapper = fragmentWrapper;
//            } else {
//                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                        "Annotation " + instanceStateType.getSimpleName() + " can be applied only to field of Activity or Fragment",
//                        field.getElement());
//                continue;
//            }

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

            GMethod onSaveInstanceStateMethod = field.getGClass().overrideMethod(executorWrapper.getSaveInstanceStateMethod(), true);
            Name bundleParam = onSaveInstanceStateMethod.getParamName(0);
            String saveCode = String.format("%s.%s(\"%s\", this.%s);",
                    bundleParam, putMethod.getSimpleName(), fieldNameInBundle, field.getName());
            JCStatement saveStatement = environment.createParser(saveCode).parseStatement();
            onSaveInstanceStateMethod.prependCode(saveStatement);

            GMethod onCreateMethod = field.getGClass().overrideMethod(executorWrapper.getRestoreInstanceStateMethod(), true);
            bundleParam = onCreateMethod.getParamName(0);
            String restoreCode;
            if (!environment.getTypes().isSameType(fieldType, serializableType) &&
                    environment.getTypes().isSubtype(fieldType, serializableType)) {
                restoreCode = String.format("this.%s = (%s) %s.%s(\"%s\");",
                        field.getName(), fieldType, bundleParam, getMethod.getSimpleName(), fieldNameInBundle);
            } else {
                restoreCode = String.format("this.%s = %s.%s(\"%s\");",
                        field.getName(), bundleParam, getMethod.getSimpleName(), fieldNameInBundle);
            }
            JCStatement restoreStatement = environment.createParser(restoreCode).parseStatement();
            onCreateMethod.prependCode(restoreStatement);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(INSTANCE_STATE_ANNOTATION);
    }

    private class ExecutorWrapper extends BaseClassWrapper {
        public ExecutorWrapper(JavacElements utils) {
            super(utils, INSTANCE_STATE_ANNOTATION + ".Executor");
        }

        public Symbol.MethodSymbol getSaveInstanceStateMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_saveInstanceState");
        }

        public Symbol.MethodSymbol getRestoreInstanceStateMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_restoreInstanceState");
        }
    }

}
