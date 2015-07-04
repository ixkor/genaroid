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
import net.xkor.genaroid.annotations.InstanceState;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;
import net.xkor.genaroid.wrap.BaseFragmentWrapper;
import net.xkor.genaroid.wrap.BaseUiContainerWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;

import javax.tools.Diagnostic;

public class InstanceStateProcessor implements SubProcessor {

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
        BaseFragmentWrapper fragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        BundleWrapper bundleWrapper = new BundleWrapper(environment);
        Type serializableType = utils.getTypeElement("java.io.Serializable").asType();

        for (GField field : environment.getGElementsAnnotatedWith(InstanceState.class, GField.class)) {
            JCTree.JCAnnotation annotation = field.extractAnnotation(InstanceState.class);
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

            BaseUiContainerWrapper uiContainerWrapper;
            if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
                uiContainerWrapper = activityWrapper;
            } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())) {
                uiContainerWrapper = supportFragmentWrapper;
            } else if (field.getGClass().isSubClass(fragmentWrapper.getClassSymbol())) {
                uiContainerWrapper = fragmentWrapper;
            } else {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + InstanceState.class.getSimpleName() + " can be applied only to field of Activity or Fragment",
                        field.getElement());
                continue;
            }

            GMethod onSaveInstanceStateMethod = field.getGClass().overrideMethod(uiContainerWrapper.getOnSaveInstanceStateMethod(), true);
            Name bundleParam = onSaveInstanceStateMethod.getParamName(0);
            String saveCode = String.format("%s.%s(\"%s\", this.%s);",
                    bundleParam, putMethod.getSimpleName(), fieldNameInBundle, field.getName());
            JCStatement saveStatement = environment.createParser(saveCode).parseStatement();
            onSaveInstanceStateMethod.prependCode(saveStatement);

            GMethod onCreateMethod = field.getGClass().overrideMethod(uiContainerWrapper.getOnCreateMethod(), true);
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
}
