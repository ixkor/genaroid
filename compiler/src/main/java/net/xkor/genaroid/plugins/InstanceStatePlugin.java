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

package net.xkor.genaroid.plugins;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;

import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
public class InstanceStatePlugin extends GenaroidPlugin {
    private static final String ANNOTATION_CLASS_NAME = InstanceState.class.getCanonicalName();

    @Override
    public void process() {
        JavacElements utils = getEnvironment().getUtils();
        Types types = getEnvironment().getTypes();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        BundleWrapper bundleWrapper = new BundleWrapper(getEnvironment());
        RestorableWrapper restorableWrapper = new RestorableWrapper(utils);

        Set<GField> allFields = getEnvironment().getGElementsAnnotatedWith(InstanceState.class, GField.class);
        for (GField field : allFields) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(instanceStateType);
            Type fieldType = ((Symbol.VarSymbol) field.getElement()).asType();
            Symbol.MethodSymbol putMethod = bundleWrapper.getMethodForType(fieldType, false);
            Symbol.MethodSymbol getMethod = bundleWrapper.getMethodForType(fieldType, true);
            String fieldNameInBundle = "_gen_" + field.getGClass().getName() + "_" + field.getName();

            if (putMethod == null || getMethod == null) {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can't found getter and putter for type of field " + field.getName(),
                        field.getElement());
                continue;
            }

            field.getGClass().implementInBestParent(restorableWrapper.getClassSymbol(), allFields);

            GMethod onSaveInstanceStateMethod = field.getGClass().overrideMethod(restorableWrapper.getSaveInstanceStateMethod(), true);
            onSaveInstanceStateMethod.prependCode("$p0.%s(\"%s\", this.%s);",
                    putMethod.getSimpleName(), fieldNameInBundle, field.getName());

            GMethod onCreateMethod = field.getGClass().overrideMethod(restorableWrapper.getRestoreInstanceStateMethod(), true);
            String methodName = getMethod.getSimpleName().toString();
            String template;
            if (methodName.equals("getSerializable")) {
                template = "this.%s = (%s) $p0.%s(\"%s\");";
            } else if (methodName.equals("getParcelableArray")) {
                fieldType = types.elemtype(fieldType);
                template = "this.%s = net.xkor.genaroid.Utils.castParcelableArray(%s.class, $p0.%s(\"%s\"));";
            } else {
                template = "this.%s = $p0.%3$s(\"%4$s\");";
            }
            onCreateMethod.prependCode(template, field.getName(), fieldType, methodName, fieldNameInBundle);
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
