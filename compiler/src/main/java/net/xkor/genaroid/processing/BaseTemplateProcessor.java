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

package net.xkor.genaroid.processing;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import net.xkor.genaroid.GenaroidEnvironment;
import net.xkor.genaroid.annotations.CodeTemplate;
import net.xkor.genaroid.tree.GClass;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public abstract class BaseTemplateProcessor implements SubProcessor {
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
                    String source = new String(Files.readAllBytes(Paths.get(environment.getProjectPath() + sourceFile)), Charset.forName("UTF-8"));
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

    private List<Template> getTemplates(JCTree.JCCompilationUnit unit) {
        List<Template> templates = new ArrayList<>();
        for (JCTree tree : unit.getTypeDecls()) {
            templates.add(new Template(unit, (JCTree.JCClassDecl) tree));
        }
        return templates;
    }

//    protected JCTree.JCCompilationUnit findBestTemplate(List<Template> templates, GElement gElement) {
//
//    }

    public static class Template {
        private JCTree.JCCompilationUnit compilationUnit;
        private JCTree.JCClassDecl classDecl;

        public Template(JCTree.JCCompilationUnit compilationUnit, JCTree.JCClassDecl classDecl) {
            this.compilationUnit = compilationUnit;
            this.classDecl = classDecl;
        }
    }
}
