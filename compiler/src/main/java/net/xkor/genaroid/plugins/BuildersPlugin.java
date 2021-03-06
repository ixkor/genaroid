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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.Utils;
import net.xkor.genaroid.annotations.BuilderParam;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.ActivityWrapper;
import net.xkor.genaroid.wrap.BaseClassWrapper;
import net.xkor.genaroid.wrap.BundleWrapper;
import net.xkor.genaroid.wrap.FragmentWrapper;
import net.xkor.genaroid.wrap.SupportFragmentWrapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
@GenaroidPlugin.Dependencies({GActivityPlugin.class, GFragmentPlugin.class})
public class BuildersPlugin extends GenaroidPlugin {
    private static final String ANNOTATION_CLASS_NAME = BuilderParam.class.getCanonicalName();
    private static final ClassName CLASS_TYPE_NAME = ClassName.get("java.lang", "Class");
    private static final ClassName CONTEXT_TYPE_NAME = ClassName.get("android.content", "Context");
    private static final ClassName INTENT_TYPE_NAME = ClassName.get("android.content", "Intent");
    private static final ParameterizedTypeName CLASS_T_TYPE_NAME = ParameterizedTypeName.get(CLASS_TYPE_NAME, TypeVariableName.get("T"));
    private static final ParameterizedTypeName CLASS_F_TYPE_NAME = ParameterizedTypeName.get(CLASS_TYPE_NAME, TypeVariableName.get("F"));
    private static final String BUILDER_SUFFIX_OPTION_NAME = "bundleBuilderSuffix";
    private static final String BASE_BUILDER_SUFFIX_OPTION_NAME = "bundleBaseBuilderSuffix";

    @NonNull
    private String builderBaseSuffix = "_BaseBuilder";
    @NonNull
    private String builderSuffix = "_Builder";

    private ActivityWrapper activityWrapper;
    private FragmentWrapper nativeFragmentWrapper;
    private SupportFragmentWrapper supportFragmentWrapper;
    private Symbol.ClassSymbol supportFragmentBuilderClass;
    private Symbol.ClassSymbol intentBaseBuilderWrapperClass;
    private Symbol.ClassSymbol fragmentBuilderClass;
    private Symbol.ClassSymbol bundleBaseBuilderClass;
    private GActivityPlugin activityPlugin;
    private GFragmentPlugin fragmentPlugin;

    @Override
    protected void init() {
        super.init();
        activityPlugin = getPlugin(GActivityPlugin.class);
        fragmentPlugin = getPlugin(GFragmentPlugin.class);

        String bundleBuilderSuffix = getEnvironment().getOption(BUILDER_SUFFIX_OPTION_NAME);
        if (bundleBuilderSuffix != null) {
            builderSuffix = bundleBuilderSuffix;
        }
        String bundleBaseBuilderSuffix = getEnvironment().getOption(BASE_BUILDER_SUFFIX_OPTION_NAME);
        if (bundleBaseBuilderSuffix != null) {
            builderBaseSuffix = bundleBaseBuilderSuffix;
        }
    }

    @Override
    public void process() {
        JavacElements utils = getEnvironment().getUtils();
        Symbol.ClassSymbol annotationClass = utils.getTypeElement(ANNOTATION_CLASS_NAME);
        intentBaseBuilderWrapperClass = utils.getTypeElement("net.xkor.genaroid.builders.IntentBaseBuilder");
        fragmentBuilderClass = utils.getTypeElement("net.xkor.genaroid.builders.FragmentBuilder");
        supportFragmentBuilderClass = utils.getTypeElement("net.xkor.genaroid.builders.SupportFragmentBuilder");
        bundleBaseBuilderClass = utils.getTypeElement("net.xkor.genaroid.builders.BundleBaseBuilder");
        activityWrapper = new ActivityWrapper(utils);
        nativeFragmentWrapper = new FragmentWrapper(utils);
        supportFragmentWrapper = new SupportFragmentWrapper(utils);
        BundleWrapper bundleWrapper = new BundleWrapper(getEnvironment());
        ParameterizableWrapper parameterizableWrapper = new ParameterizableWrapper(utils);

        HashMap<Symbol.ClassSymbol, BuilderClass> builders = new HashMap<>();

        Set<GField> allFields = getEnvironment().getGElementsAnnotatedWith(BuilderParam.class, GField.class);
        List<GField> sortedFields = new ArrayList<>(allFields);
        Collections.sort(sortedFields, new Comparator<GField>() {
            @Override
            public int compare(GField field1, GField field2) {
                return field1.getGClass().getHierarchyLevel() - field2.getGClass().getHierarchyLevel();
            }
        });

        HashSet<GClass> classesForBuilders = new HashSet<>();
        if (activityPlugin.getActivities() != null) {
            classesForBuilders.addAll(activityPlugin.getActivities());
        }
        if (fragmentPlugin.getFragments() != null) {
            classesForBuilders.addAll(fragmentPlugin.getFragments());
        }
        for (GField field : allFields) {
            classesForBuilders.add(field.getGClass());
        }
        ArrayList<GClass> sortedClassesForBuilders = new ArrayList<>(classesForBuilders);
        Collections.sort(sortedClassesForBuilders, GClass.HIERARCHY_LEVEL_COMPARATOR);

        for (GClass gClass : sortedClassesForBuilders) {
            Symbol.ClassSymbol currentClass = gClass.getElement();
            BuilderClass superBuilder = null;
            while (currentClass != getEnvironment().getObjectClass() && superBuilder == null) {
                currentClass = (Symbol.ClassSymbol) currentClass.getSuperclass().asElement();
                superBuilder = builders.get(currentClass);
            }

            BuilderClass builder = new BuilderClass(gClass.getElement(), superBuilder);
            builder.init(gClass);
            builders.put(gClass.getElement(), builder);
        }

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
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + annotationClass.getSimpleName() + " can be applied only to fields of classes annotated by GFragment or GActivity",
                        field.getElement());
                continue;
            }

            field.getGClass().implementInBestParent(parameterizableWrapper.getClassSymbol(), allFields);

            GMethod readParamsMethod = field.getGClass().overrideMethod(parameterizableWrapper.getReadParamsMethod(), true);
            Name bundleParam = readParamsMethod.getParamName(0);
            JCTree.JCStatement restoreStatement = bundleWrapper.getReadStatement(field, fieldNameInBundle, bundleParam);
            readParamsMethod.appendCode(restoreStatement);

            Symbol.MethodSymbol putMethod = bundleWrapper.getMethodForType(fieldType, false);
            if (putMethod == null) {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can't found getter and putter for type of field " + field.getName(),
                        field.getElement());
                continue;
            }
            String putMethodName = putMethod.getSimpleName().toString();

            builder.addParameter(field, fieldNameInBundle, fieldType, putMethodName, isOptional);
        }

        for (BuilderClass builder : builders.values()) {
            builder.writeToFile(getEnvironment());
        }
    }

    @NonNull
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }

    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> options = new HashSet<>();
        options.add(BUILDER_SUFFIX_OPTION_NAME);
        options.add(BASE_BUILDER_SUFFIX_OPTION_NAME);
        return options;
    }

    private static class ParameterizableWrapper extends BaseClassWrapper {
        public ParameterizableWrapper(JavacElements utils) {
            super(utils, "net.xkor.genaroid.internal.Parameterizable");
        }

        public Symbol.MethodSymbol getReadParamsMethod() {
            return (Symbol.MethodSymbol) getMember("_gen_readParams");
        }
    }

    private static class MethodParameter {
        private final TypeName typeName;
        private final String name;
        private final String putMethodName;
        private final String fieldNameInBundle;

        public MethodParameter(TypeName typeName, String name, String putMethodName, String fieldNameInBundle) {
            this.typeName = typeName;
            this.name = name;
            this.fieldNameInBundle = fieldNameInBundle;
            this.putMethodName = putMethodName;
        }
    }

    private class BuilderClass {
        private final BuilderClass superBuilder;

        private final TypeSpec.Builder classBaseBuilder;
        private final TypeSpec.Builder classBuilder;
        private final MethodSpec.Builder publicConstructorBuilder;
        private final MethodSpec.Builder protectedConstructorBuilder;

        private final String packageName;
        private final String baseName;
        private final String name;

        private final ArrayList<MethodParameter> constructorParams = new ArrayList<>();
        private final boolean isAbstract;

        public BuilderClass(Symbol.ClassSymbol classSymbol, BuilderClass superBuilder) {
            this.superBuilder = superBuilder;
            isAbstract = (classSymbol.flags() & Flags.ABSTRACT) != 0;

            packageName = classSymbol.packge().toString();
            String className = getClassName(classSymbol);
            baseName = className + builderBaseSuffix;
            name = className + builderSuffix;

            classBaseBuilder = TypeSpec.classBuilder(baseName).addModifiers(Modifier.PUBLIC);
            classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
            publicConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode("$[");
            protectedConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED).addCode("$[");
        }

        @NonNull
        private String getClassName(Symbol classSymbol) {
            String name = classSymbol.getSimpleName().toString();
            classSymbol = classSymbol.getEnclosingElement();
            while (!(classSymbol instanceof Symbol.PackageSymbol)) {
                name = classSymbol.getSimpleName().toString() + "_" + name;
                classSymbol = classSymbol.getEnclosingElement();
            }
            return name;
        }

        public boolean init(GClass fieldGClass) {
            if (fieldGClass.isSubClass(activityWrapper.getClassSymbol())) {
                initActivityBuilder(fieldGClass.getElement());
            } else if (fieldGClass.isSubClass(supportFragmentWrapper.getClassSymbol())) {
                initFragmentBuilder(supportFragmentBuilderClass, fieldGClass.getElement());
            } else if (fieldGClass.isSubClass(nativeFragmentWrapper.getClassSymbol())) {
                initFragmentBuilder(fragmentBuilderClass, fieldGClass.getElement());
            } else {
                initBaseBundleBuilder(fieldGClass.getElement());
            }
            return true;
        }

        public void initBaseBundleBuilder(Symbol.ClassSymbol classSymbol) {
            ClassName baseBuilderClassName;
            if (superBuilder == null) {
                baseBuilderClassName = ClassName.get(bundleBaseBuilderClass);
            } else {
                baseBuilderClassName = ClassName.get(superBuilder.packageName, superBuilder.baseName);
            }

            classBaseBuilder
                    .addTypeVariable(TypeVariableName.get("T", ParameterizedTypeName.get(ClassName.get(packageName, baseName), TypeVariableName.get("T"))))
                    .superclass(ParameterizedTypeName.get(baseBuilderClassName, TypeVariableName.get("T")));
            classBuilder
                    .superclass(ParameterizedTypeName.get(ClassName.get(packageName, baseName), ClassName.get(packageName, name)));
            protectedConstructorBuilder
                    .addParameter(CLASS_T_TYPE_NAME, "builderClass");

            protectedConstructorBuilder.addCode("super(builderClass");
            publicConstructorBuilder.addCode("super($T.class", ClassName.get(packageName, name));
        }

        public void initActivityBuilder(Symbol.ClassSymbol activityClassSymbol) {
            ClassName baseBuilderClassName;
            if (superBuilder == null) {
                baseBuilderClassName = ClassName.get(intentBaseBuilderWrapperClass);
            } else {
                baseBuilderClassName = ClassName.get(superBuilder.packageName, superBuilder.baseName);
            }

            classBaseBuilder
                    .addTypeVariable(TypeVariableName.get("T", ParameterizedTypeName.get(ClassName.get(packageName, baseName), TypeVariableName.get("T"))))
                    .superclass(ParameterizedTypeName.get(baseBuilderClassName, TypeVariableName.get("T")));
            classBuilder
                    .superclass(ParameterizedTypeName.get(ClassName.get(packageName, baseName), ClassName.get(packageName, name)));
            protectedConstructorBuilder
                    .addParameter(INTENT_TYPE_NAME, "intent")
                    .addParameter(CLASS_T_TYPE_NAME, "builderClass");
            publicConstructorBuilder
                    .addParameter(CONTEXT_TYPE_NAME, "context");

            protectedConstructorBuilder.addCode("super(intent, builderClass");
            publicConstructorBuilder.addCode("super(new $T(context, $T.class), $T.class", INTENT_TYPE_NAME, ClassName.get(activityClassSymbol), ClassName.get(packageName, name));
        }

        public void initFragmentBuilder(Symbol.ClassSymbol baseBuilderWrapperClass, Symbol.ClassSymbol fragmentClassSymbol) {
            ClassName baseBuilderClassName;
            if (superBuilder == null) {
                baseBuilderClassName = ClassName.get(baseBuilderWrapperClass);
            } else {
                baseBuilderClassName = ClassName.get(superBuilder.packageName, superBuilder.baseName);
            }

            classBaseBuilder
                    .addTypeVariable(TypeVariableName.get("F", ClassName.get(fragmentClassSymbol)))
                    .addTypeVariable(TypeVariableName.get("T", ParameterizedTypeName.get(ClassName.get(packageName, baseName), TypeVariableName.get("F"), TypeVariableName.get("T"))))
                    .superclass(ParameterizedTypeName.get(baseBuilderClassName, TypeVariableName.get("F"), TypeVariableName.get("T")));
            classBuilder
                    .superclass(ParameterizedTypeName.get(ClassName.get(packageName, baseName), ClassName.get(fragmentClassSymbol), ClassName.get(packageName, name)));
            protectedConstructorBuilder
                    .addParameter(CLASS_F_TYPE_NAME, "fragmentClass")
                    .addParameter(CLASS_T_TYPE_NAME, "builderClass");

            protectedConstructorBuilder.addCode("super(fragmentClass, builderClass");
            publicConstructorBuilder.addCode("super($T.class, $T.class", ClassName.get(fragmentClassSymbol), ClassName.get(packageName, name));
        }

        public void addParameter(GField field, String fieldNameInBundle, Type fieldType, String putMethodName, boolean optional) {
            if (optional) {
                MethodSpec putter = MethodSpec.methodBuilder(field.getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.get(fieldType), field.getName())
                        .addStatement("getBundle().$L($L, $L)", putMethodName, fieldNameInBundle, field.getName())
                        .addStatement("return builderClass.cast(this)")
                        .returns(TypeVariableName.get("T"))
                        .build();
                classBaseBuilder.addMethod(putter);
            } else {
                constructorParams.add(new MethodParameter(TypeName.get(fieldType), field.getName(), putMethodName, fieldNameInBundle));
            }
        }

        public void writeToFile(GenaroidEnvironment environment) {
            if (superBuilder != null) {
                for (MethodParameter parameter : superBuilder.constructorParams) {
                    protectedConstructorBuilder
                            .addParameter(parameter.typeName, parameter.name)
                            .addCode(", " + parameter.name);
                    publicConstructorBuilder
                            .addParameter(parameter.typeName, parameter.name)
                            .addCode(", " + parameter.name);
                }
            }
            protectedConstructorBuilder.addCode(");\n$]");

            for (MethodParameter parameter : constructorParams) {
                protectedConstructorBuilder
                        .addParameter(parameter.typeName, parameter.name)
                        .addStatement("getBundle().$L($L, $L)", parameter.putMethodName, parameter.fieldNameInBundle, parameter.name);
                publicConstructorBuilder
                        .addParameter(parameter.typeName, parameter.name)
                        .addCode(", " + parameter.name);
            }
            publicConstructorBuilder.addCode(");\n$]");

            if (!isAbstract) {
                writeToFile(environment, classBuilder.addMethod(publicConstructorBuilder.build()).build());
            }
            writeToFile(environment, classBaseBuilder.addMethod(protectedConstructorBuilder.build()).build());
        }

        private void writeToFile(GenaroidEnvironment environment, TypeSpec builderClass) {
            JavaFile javaFile = JavaFile.builder(packageName, builderClass).build();
            try {
                javaFile.writeTo(environment.getJavacProcessingEnv().getFiler());
            } catch (IOException e) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("Can't write builder %s.%s to file: %s", packageName, builderClass.name, Utils.getStackTrace(e)));
            }
        }
    }
}
