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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.BaseFragmentWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

public class CreateParamProcessor implements SubProcessor {
    private static final String ANNOTATION_CLASS_NAME = "net.xkor.genaroid.annotations.CreateParam";

    @Override
    public void process(GenaroidEnvironment environment) {
        JavacElements utils = environment.getUtils();
        Types types = environment.getTypes();
        Symbol.ClassSymbol annotationClass = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
        BaseFragmentWrapper nativeFragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        BundleWrapper bundleWrapper = new BundleWrapper(environment);
        ParameterizableWrapper parameterizableWrapper = new ParameterizableWrapper(utils);
        BaseClassWrapper intentBuilderWrapper = new BaseClassWrapper(utils, "net.xkor.genaroid.IntentBuilder");
        BaseClassWrapper fragmentBuilderWrapper = new BaseClassWrapper(utils, "net.xkor.genaroid.FragmentBuilder");
        BaseClassWrapper supportFragmentBuilderWrapper = new BaseClassWrapper(utils, "net.xkor.genaroid.SupportFragmentBuilder");
        BaseClassWrapper contextWrapper = new BaseClassWrapper(utils, "android.content.Context");

        HashMap<String, TypeSpec.Builder> generatedClasses = new HashMap<>();
        HashMap<String, MethodSpec.Builder> generatedConstructors = new HashMap<>();

        Set<GField> allFields = environment.getGElementsAnnotatedWith(annotationClass, GField.class);
        for (GField field : allFields) {
            field.extractAnnotation(annotationClass);
            AnnotationMirror annotationMirror = field.findAnnotationMirror(annotationClass.asType());
            String fieldNameInBundle = "\"" + field.getName() + "\"";
            boolean isOptional = false;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> arg : annotationMirror.getElementValues().entrySet()) {
                String argName = arg.getKey().toString();
                String argValue = arg.getValue().toString();
                if (argName.equals("value()")) {
                    fieldNameInBundle = argValue;
                } else if (argName.equals("isOptional()")) {
                    isOptional = Boolean.parseBoolean(argValue);
                }
            }

            Type fieldType = ((Symbol.VarSymbol) field.getElement()).asType();
            String builderClassPackage = field.getGClass().getElement().packge().toString();
            String builderClassName = field.getGClass().getElement().getSimpleName().toString() + "Builder";
            String builderClassFullName = builderClassPackage + "." + builderClassName;
            TypeSpec.Builder builderClassBuilder = generatedClasses.get(builderClassFullName);
            MethodSpec.Builder builderClassConstructorBuilder = generatedConstructors.get(builderClassFullName);
            if (builderClassBuilder == null) {
                builderClassConstructorBuilder = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC);
                generatedConstructors.put(builderClassFullName, builderClassConstructorBuilder);

                builderClassBuilder = TypeSpec.classBuilder(builderClassName)
                        .addModifiers(Modifier.PUBLIC);
                generatedClasses.put(builderClassFullName, builderClassBuilder);

                TypeName fieldClassTypeName = TypeName.get(field.getGClass().getElement().asType());
                if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
                    builderClassBuilder.superclass(TypeName.get(intentBuilderWrapper.getClassSymbol().asType()));
                    builderClassConstructorBuilder
                            .addParameter(TypeName.get(contextWrapper.getClassSymbol().asType()), "context")
                            .addStatement("super(context, $T.class)", fieldClassTypeName);
                } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())) {
                    builderClassBuilder.superclass(ParameterizedTypeName.get(
                            ClassName.get(supportFragmentBuilderWrapper.getClassSymbol()), fieldClassTypeName));
                    builderClassConstructorBuilder
                            .addStatement("super($T.class)", fieldClassTypeName);
                } else if (field.getGClass().isSubClass(nativeFragmentWrapper.getClassSymbol())) {
                    builderClassBuilder.superclass(ParameterizedTypeName.get(
                            ClassName.get(fragmentBuilderWrapper.getClassSymbol()), fieldClassTypeName));
                    builderClassConstructorBuilder
                            .addStatement("super($T.class)", fieldClassTypeName);
                } else {
                    environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Annotation " + annotationClass.getSimpleName() + " can be applied only to subclasses of Fragment or Activity",
                            field.getElement());
                    continue;
                }
            }

            field.getGClass().implementInBestParent(parameterizableWrapper.getClassSymbol(), allFields);

            GMethod onCreateMethod = field.getGClass().overrideMethod(parameterizableWrapper.getReadParamsMethod(), true);
            Name bundleParam = onCreateMethod.getParamName(0);
            JCTree.JCStatement restoreStatement = bundleWrapper.getReadStatement(field, fieldNameInBundle, bundleParam);
            onCreateMethod.prependCode(restoreStatement);

            Symbol.MethodSymbol putMethod = bundleWrapper.getMethodForType(fieldType, false);
            if (putMethod == null) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can't found getter and putter for type of field " + field.getName(),
                        field.getElement());
                continue;
            }
            String putMethodName = putMethod.getSimpleName().toString();

            if (isOptional) {
                MethodSpec putter = MethodSpec.methodBuilder(field.getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName())
                        .addStatement("return this")
                        .returns(ClassName.get(builderClassPackage, builderClassName))
                        .build();

                builderClassBuilder.addMethod(putter);
            } else {
                builderClassConstructorBuilder
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName());
            }
        }

        for (Map.Entry<String, TypeSpec.Builder> entry : generatedClasses.entrySet()) {
            MethodSpec constructor = generatedConstructors.get(entry.getKey()).build();
            TypeSpec builderClass = entry.getValue().addMethod(constructor).build();

            String packageName = entry.getKey().replace("." + builderClass.name, "");
            JavaFile javaFile = JavaFile.builder(packageName, builderClass).build();
            try {
                javaFile.writeTo(environment.getJavacProcessingEnv().getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }

    private class ParameterizableWrapper extends BaseClassWrapper {
        public ParameterizableWrapper(JavacElements utils) {
            super(utils, "net.xkor.genaroid.internal.Parameterizable");
        }

        public Symbol.MethodSymbol getReadParamsMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_readParams");
        }
    }
}
