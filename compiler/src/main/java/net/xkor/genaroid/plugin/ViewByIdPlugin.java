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
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.ViewById;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.wrap.BindableWrapper;
import net.xkor.genaroid.wrap.ViewWrapper;

import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
public class ViewByIdPlugin extends GenaroidPlugin {
    private static final String ANNOTATION_CLASS_NAME = ViewById.class.getCanonicalName();

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Symbol.ClassSymbol viewByIdType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        ViewWrapper viewWrapper = new ViewWrapper(utils);
        BindableWrapper bindableWrapper = new BindableWrapper(utils);

        Set<GField> allFields = environment.getGElementsAnnotatedWith(ViewById.class, GField.class);
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

            field.getGClass().implementInBestParent(bindableWrapper.getClassSymbol(), allFields);

            field.getGClass().overrideMethod(bindableWrapper.getBindMethod(), true)
                    .prependCode("this.%s = (%s) $p0.findViewById(%s);", field.getName(), fieldType, value);
            field.getGClass().overrideMethod(bindableWrapper.getUnbindMethod(), true)
                    .prependCode("this.%s = null;", field.getName());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }
}
