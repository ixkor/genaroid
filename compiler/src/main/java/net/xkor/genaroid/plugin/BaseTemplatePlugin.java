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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.CodeTemplate;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GClassMember;
import net.xkor.genaroid.tree.GElement;
import net.xkor.genaroid.tree.GField;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

public abstract class BaseTemplatePlugin extends GenaroidPlugin {
    public static final String TAG_FIELD_TYPE = "$fieldType$";
    public static final String TAG_FIELD = "$field$";
    public static final String TAG_FIELD_NAME = "$FIELD_NAME$";
    public static final String TAG_METHOD_RETURN_TYPE = "$methodReturnType$";
    public static final String TAG_METHOD = "$method$";
    public static final String TAG_METHOD_NAME = "$METHOD_NAME$";
    public static final String TAG_ANNOTATION = "$annotation$";

    private GenaroidEnvironment environment;

    @Override
    public void process(GenaroidEnvironment environment) {
        this.environment = environment;
    }

    protected List<Template> getTemplates(Element annotation) {
        List<Template> templates = new ArrayList<>();
        CodeTemplate codeTemplate = annotation.getAnnotation(CodeTemplate.class);
        if (codeTemplate.sources().length > 0) {
            for (String source : codeTemplate.sources()) {
                templates.addAll(getTemplates(environment.parseUnit(source)));
            }
        } else {
            Symbol.ClassSymbol codeTemplateType = environment.getUtils().getTypeElement(CodeTemplate.class.getCanonicalName());
            GClass gClass = GClass.getGClass(environment, annotation);
            JCTree.JCAnnotation codeTemplateAnnotation = gClass.getAnnotation(codeTemplateType);
            for (String sourceFile : codeTemplate.value()) {
                try {
                    String source = new String(Files.readAllBytes(Paths.get(environment.getProjectPath(), sourceFile)), Charset.forName("UTF-8"));
                    templates.addAll(getTemplates(environment.parseUnit(source)));
                    if (environment.isSaveTemplates()) {
//                        codeTemplateAnnotation.args.
                    }
                } catch (IOException e) {
                    environment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
                }
            }
        }
        return templates;
    }

    protected Template findBestTemplate(List<Template> templates, GElement gElement) {
        GClass gClass;
        if (gElement instanceof GClassMember) {
            gClass = ((GClassMember) gElement).getGClass();
        } else {
            gClass = (GClass) gElement;
        }
        if (gElement instanceof GField) {
//            gElement.getElement().asType()
        }

        for (Template template : templates) {
            if (template.extendingClass != null && !gClass.isSubClass(template.extendingClass)) {
                continue;
            }

            return template;
        }

        return null;
    }

    private List<Template> getTemplates(JCTree.JCCompilationUnit unit) {
        List<Template> templates = new ArrayList<>();
        for (JCTree tree : unit.getTypeDecls()) {
            templates.add(new Template(unit, (JCTree.JCClassDecl) tree));
        }
        return templates;
    }

    public class Template {
        private JCTree.JCCompilationUnit compilationUnit;
        private JCTree.JCClassDecl classDecl;
        private Symbol.TypeSymbol fieldClass;
        private Symbol.TypeSymbol annotationClass;
        private Symbol.TypeSymbol extendingClass;
        private List<Symbol.TypeSymbol> implementingClasses;

        public Template(JCTree.JCCompilationUnit compilationUnit, JCTree.JCClassDecl classDecl) {
            this.compilationUnit = compilationUnit;
            this.classDecl = classDecl;

            for (JCTree.JCTypeParameter typeParameter : classDecl.typarams) {
                if (TAG_FIELD_TYPE.equals(typeParameter.name.toString()) && typeParameter.bounds.nonEmpty()) {
                    fieldClass = findType(typeParameter.bounds.get(0));
                }
            }

            if (classDecl.extending != null) {
                extendingClass = findType(classDecl.extending);
            }

            if (classDecl.implementing != null) {
                implementingClasses = new ArrayList<>();
                for (JCTree.JCExpression impl : classDecl.implementing) {
                    implementingClasses.add(findType(impl));
                }
            }

            if (classDecl.defs != null) {
                com.sun.tools.javac.util.List<JCTree> newDefs = com.sun.tools.javac.util.List.nil();
                for (JCTree member : classDecl.defs) {
                    if (member instanceof JCTree.JCVariableDecl) {
                        JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) member;
                        switch (variableDecl.name.toString()) {
                            case TAG_FIELD:
                                fieldClass = findType(variableDecl.vartype);
                                continue;
                            case TAG_ANNOTATION:
                                annotationClass = findType(variableDecl.vartype);
                                continue;
                        }
                    }
                    newDefs = newDefs.append(member);
                }
                classDecl.defs = newDefs;
            }
        }

        private Symbol.TypeSymbol findType(JCTree.JCExpression expression) {
            if (expression instanceof JCTree.JCPrimitiveTypeTree) {
                TypeKind primitiveTypeKind = ((JCTree.JCPrimitiveTypeTree) expression).getPrimitiveTypeKind();
                Type primitiveType = (Type) environment.getTypeUtils().getPrimitiveType(primitiveTypeKind);
                return primitiveType.asElement();
            }

            return environment.getUtils().getTypeElement(expression.toString());
//            String typeFullName = expression.toString();
//            String typeBase = typeFullName.replaceFirst("[\\.<].*$", "");
//            String typeEnd = typeFullName.replaceFirst("^[\\w]+", "");
//
//            for (JCTree.JCImport jcImport : compilationUnit.getImports()) {
//                String importString = jcImport.getQualifiedIdentifier().toString();
//                String importedType = importString.replaceFirst("^[\\w\\.]*\\.", "");
//                if (typeBase.equals(importedType)) {
//                    environment.getUtils().getTypeElement(importString + typeEnd);
//                }
//            }
        }
    }
}
