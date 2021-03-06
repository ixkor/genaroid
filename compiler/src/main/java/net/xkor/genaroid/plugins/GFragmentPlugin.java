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

import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.GFragment;
import net.xkor.genaroid.annotations.InjectGenaroidCall;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BaseFragmentWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.InflatableWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
public class GFragmentPlugin extends GenaroidPlugin {

    private static final String ANNOTATION_CLASS_NAME = GFragment.class.getCanonicalName();

    private ArrayList<GClass> sortedFragments;

    public java.util.List<GClass> getFragments() {
        return sortedFragments;
    }

    @Override
    public void process() {
        JavacElements utils = getEnvironment().getUtils();
        Symbol.ClassSymbol instanceStateType = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        BaseFragmentWrapper nativeFragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        InflatableWrapper inflatableWrapper = new InflatableWrapper(utils);

        Set<GClass> fragments = getEnvironment().getGElementsAnnotatedWith(GFragment.class, GClass.class);
        sortedFragments = new ArrayList<>(fragments);
        Collections.sort(sortedFragments, GClass.HIERARCHY_LEVEL_COMPARATOR);

        for (GClass fragment : sortedFragments) {
            JCTree.JCAnnotation jcAnnotation = fragment.extractAnnotation(instanceStateType);
            GFragment annotation = fragment.getAnnotation(GFragment.class);

            BaseFragmentWrapper fragmentWrapper;
            if (fragment.isSubClass(supportFragmentWrapper.getClassSymbol())) {
                fragmentWrapper = supportFragmentWrapper;
            } else if (fragment.isSubClass(nativeFragmentWrapper.getClassSymbol())) {
                fragmentWrapper = nativeFragmentWrapper;
            } else {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + instanceStateType.getSimpleName() + " can be applied only to subclasses of Fragment",
                        fragment.getElement());
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

                fragment.implementIfNeeded(inflatableWrapper.getClassSymbol());
                fragment.overrideMethod(inflatableWrapper.getGetLayoutIdMethod(), false)
                        .appendCode("return %s;", layoutId);
            }

            if (fragment.isBaseWithAnnotation(GFragment.class)) {
                fragment.getGUnit().addNewImports(GenaroidEnvironment.GENAROID_MAIN_CLASS);
                GMethod onCreateMethod = fragment.overrideMethod(fragmentWrapper.getOnCreateMethod(), true);

                if ((annotation.injectCalls() & InjectGenaroidCall.INSTANCE_STATE) != 0) {
                    fragment.overrideMethod(fragmentWrapper.getOnSaveInstanceStateMethod(), true)
                            .appendCode("Genaroid.saveInstanceState(this, $p0);");
                    onCreateMethod.prependCode("Genaroid.restoreInstanceState(this, $p0);");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.BIND) != 0) {
                    fragment.overrideMethod(fragmentWrapper.getOnViewCreatedMethod(), true)
                            .appendCode("Genaroid.bind(this, $p0);");
                    fragment.overrideMethod(fragmentWrapper.getOnDestroyViewMethod(), true)
                            .appendCode("Genaroid.unbind(this);");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.READ_PARAMS) != 0) {
                    onCreateMethod.prependCode("Genaroid.readParams(this);");
                }

                if ((annotation.injectCalls() & InjectGenaroidCall.INFLATE_LAYOUT) != 0) {
                    GMethod onCreateViewMethod = fragment.overrideMethod(fragmentWrapper.getOnCreateViewMethod(), false);
                    if (onCreateViewMethod.getBody().size() > 0) {
                        getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "You can not override method onCreateView when the injectCalls parameter of GFragment annotation contains InjectGenaroidCall.INFLATE_LAYOUT value",
                                fragment.getElement());
                        continue;
                    }
                    onCreateViewMethod.appendCode("return Genaroid.inflate(this, $p0, $p1);");
                }
            }
        }
    }

    @NonNull
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }
}
