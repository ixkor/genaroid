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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
        Symbol.ClassSymbol annotationClass = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        Symbol.ClassSymbol intentBuilderWrapperClass = utils.getTypeElement("net.xkor.genaroid.IntentBuilder");
        Symbol.ClassSymbol fragmentBuilderClass = utils.getTypeElement("net.xkor.genaroid.FragmentBuilder");
        Symbol.ClassSymbol supportFragmentBuilderClass = utils.getTypeElement("net.xkor.genaroid.SupportFragmentBuilder");
        ActivityWrapper activityWrapper = new ActivityWrapper(utils);
        BaseFragmentWrapper nativeFragmentWrapper = new FragmentWrapper(utils);
        BaseFragmentWrapper supportFragmentWrapper = new SupportFragmentWrapper(utils);
        BundleWrapper bundleWrapper = new BundleWrapper(environment);
        ParameterizableWrapper parameterizableWrapper = new ParameterizableWrapper(utils);
        BaseClassWrapper contextWrapper = new BaseClassWrapper(utils, "android.content.Context");

        HashMap<Symbol.ClassSymbol, BuilderClass> builders = new HashMap<>();

        Set<GField> allFields = environment.getGElementsAnnotatedWith(annotationClass, GField.class);
        List<GField> sortedFields = new ArrayList<>(allFields);
        Collections.sort(sortedFields, new Comparator<GField>() {
            @Override
            public int compare(GField field1, GField field2) {
                return field1.getGClass().getHierarchyLevel() - field2.getGClass().getHierarchyLevel();
            }
        });

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
                } else if (argName.equals("optional()")) {
                    isOptional = Boolean.parseBoolean(argValue);
                }
            }

            Type fieldType = ((Symbol.VarSymbol) field.getElement()).asType();
            Symbol.ClassSymbol fieldClassSymbol = field.getGClass().getElement();
            BuilderClass builder = builders.get(fieldClassSymbol);
            if (builder == null) {
                builder = new BuilderClass(fieldClassSymbol);
                builders.put(fieldClassSymbol, builder);

                TypeName fieldClassTypeName = TypeName.get(fieldClassSymbol.asType());
                if (field.getGClass().isSubClass(activityWrapper.getClassSymbol())) {
                    builder.classBuilder.superclass(TypeName.get(intentBuilderWrapperClass.asType()));
                    builder.publicConstructorBuilder
                            .addParameter(TypeName.get(contextWrapper.getClassSymbol().asType()), "context")
                            .addStatement("super(context, $T.class)", fieldClassTypeName);
                } else if (field.getGClass().isSubClass(supportFragmentWrapper.getClassSymbol())) {
                    builder.classBuilder.superclass(ParameterizedTypeName.get(
                            ClassName.get(supportFragmentBuilderClass), fieldClassTypeName));
                    builder.publicConstructorBuilder
                            .addStatement("super($T.class)", fieldClassTypeName);
                } else if (field.getGClass().isSubClass(nativeFragmentWrapper.getClassSymbol())) {
                    builder.classBuilder.superclass(ParameterizedTypeName.get(
                            ClassName.get(fragmentBuilderClass), fieldClassTypeName));
                    builder.publicConstructorBuilder
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

            builder.addParameter(field, fieldNameInBundle, fieldType, putMethodName, isOptional);
        }

        for (BuilderClass builder : builders.values()) {
            MethodSpec constructor = builder.publicConstructorBuilder.build();
            TypeSpec builderClass = builder.classBuilder.addMethod(constructor).build();

            JavaFile javaFile = JavaFile.builder(builder.classPackage, builderClass).build();
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

    private static class ParameterizableWrapper extends BaseClassWrapper {
        public ParameterizableWrapper(JavacElements utils) {
            super(utils, "net.xkor.genaroid.internal.Parameterizable");
        }

        public Symbol.MethodSymbol getReadParamsMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_readParams");
        }
    }

    private static class BuilderClass {
        TypeSpec.Builder classBuilder;
        MethodSpec.Builder publicConstructorBuilder;
        MethodSpec.Builder protectedConstructorBuilder;

        String classPackage;
        String name;
        String fullName;

        public BuilderClass(Symbol.ClassSymbol classSymbol) {
            classPackage = classSymbol.packge().toString();
            name = classSymbol.getSimpleName().toString() + "Builder";
            fullName = classPackage + "." + name;

            classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
            publicConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
            protectedConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED);
        }

        public void addParameter(GField field, String fieldNameInBundle, Type fieldType, String putMethodName, boolean optional) {
            if (optional) {
                MethodSpec putter = MethodSpec.methodBuilder(field.getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName())
                        .addStatement("return this")
                        .returns(ClassName.get(classPackage, name))
                        .build();
                classBuilder.addMethod(putter);
            } else {
                publicConstructorBuilder
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName());
                protectedConstructorBuilder
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName());
            }
        }
    }
}
