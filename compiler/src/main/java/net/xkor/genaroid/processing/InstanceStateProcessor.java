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
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

public class InstanceStateProcessor implements SubProcessor {
    private static final String ANNOTATION_CLASS_NAME = "net.xkor.genaroid.annotations.InstanceState";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Types types = environment.getTypes();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        BundleWrapper bundleWrapper = new BundleWrapper(environment);
        RestorableWrapper restorableWrapper = new RestorableWrapper(utils);

        Set<GField> allFields = environment.getGElementsAnnotatedWith(instanceStateType, GField.class);
        for (GField field : allFields) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(instanceStateType);
            Type fieldType = ((Symbol.VarSymbol) field.getElement()).asType();
            Symbol.MethodSymbol putMethod = bundleWrapper.getMethodForType(fieldType, false);
            Symbol.MethodSymbol getMethod = bundleWrapper.getMethodForType(fieldType, true);
            String fieldNameInBundle = "_gen_" + field.getGClass().getName() + "_" + field.getName();

            if (putMethod == null || getMethod == null) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can't found getter and putter for type of field " + field.getName(),
                        field.getElement());
                continue;
            }

            field.getGClass().implementInBestParent(restorableWrapper.getClassSymbol(), allFields);

            GMethod onSaveInstanceStateMethod = field.getGClass().overrideMethod(restorableWrapper.getSaveInstanceStateMethod(), true);
            Name bundleParam = onSaveInstanceStateMethod.getParamName(0);
            String saveCode = String.format("%s.%s(\"%s\", this.%s);",
                    bundleParam, putMethod.getSimpleName(), fieldNameInBundle, field.getName());
            JCStatement saveStatement = environment.createParser(saveCode).parseStatement();
            onSaveInstanceStateMethod.prependCode(saveStatement);

            GMethod onCreateMethod = field.getGClass().overrideMethod(restorableWrapper.getRestoreInstanceStateMethod(), true);
            bundleParam = onCreateMethod.getParamName(0);
            String methodName = getMethod.getSimpleName().toString();
            String template;
            if (methodName.equals("getSerializable")) {
                template = "this.%s = (%s) %s.%s(\"%s\");";
            } else if (methodName.equals("getParcelableArray")) {
                fieldType = types.elemtype(fieldType);
                template = "this.%s = net.xkor.genaroid.Utils.castParcelableArray(%s.class, %s.%s(\"%s\"));";
            } else {
                template = "this.%s = %3$s.%4$s(\"%5$s\");";
            }
            String restoreCode = String.format(
                    template, field.getName(), fieldType, bundleParam, methodName, fieldNameInBundle);
            JCStatement restoreStatement = environment.createParser(restoreCode).parseStatement();
            onCreateMethod.prependCode(restoreStatement);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }

    private class RestorableWrapper extends BaseClassWrapper {
        public RestorableWrapper(JavacElements utils) {
            super(utils, "net.xkor.genaroid.internal.Restorable");
        }

        public Symbol.MethodSymbol getSaveInstanceStateMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_saveInstanceState");
        }

        public Symbol.MethodSymbol getRestoreInstanceStateMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_restoreInstanceState");
        }
    }

}
