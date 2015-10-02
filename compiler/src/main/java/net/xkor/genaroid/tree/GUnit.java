/*
 * Copyright (C) 2015 Aleksei Skoriatin
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
package net.xkor.genaroid.tree;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import net.xkor.genaroid.GenaroidEnvironment;

import java.util.Collection;
import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class GUnit {
    private static final String ANONYMOUS_CLASS_PREFIX = "__fake__";

    private JCCompilationUnit compilationUnit;
    private GenaroidEnvironment environment;
    private HashMap<String, GClass> classes = new HashMap<>();

    public GUnit(JCCompilationUnit compilationUnit, GenaroidEnvironment environment) {
        this.compilationUnit = compilationUnit;
        this.environment = environment;
    }

    @SafeVarargs
    private static List<JCImport> mergeImports(List<JCImport> mergedImports, List<JCImport>... importLists) {
        for (List<JCImport> imports : importLists) {
            for (JCImport imp : imports) {
                mergedImports = appendImport(mergedImports, imp);
            }
        }
        return mergedImports;
    }

    private static List<JCImport> appendImport(List<JCImport> imports, JCImport addImport) {
        for (JCImport imp : imports) {
            if (imp.toString().equals(addImport.toString())) {
                return imports;
            }
        }
        return imports.append(addImport);
    }

    public static GUnit getGUnit(GenaroidEnvironment environment, Element element) {
        Pair<JCTree, JCCompilationUnit> pair = environment.getTreeAndTopLevel(element);
        GUnit unit = environment.getUnit(pair.snd.getSourceFile().getName());
        if (unit == null) {
            unit = new GUnit(pair.snd, environment);
            environment.putUnit(unit);
        }
        return unit;
    }

    public Collection<GClass> getGClasses() {
        return classes.values();
    }

    public GClass getGClass(String className) {
        return classes.get(className);
    }

    public void putGClass(GClass gClass) {
        classes.put(gClass.getName(), gClass);
    }

    public JCCompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public GenaroidEnvironment getEnvironment() {
        return environment;
    }

    public GClass createAnonymousClass() {
        TreeMaker maker = environment.getMaker();
        JCClassDecl classDecl = maker.AnonymousClassDef(maker.Modifiers(0), List.<JCTree>nil());
        return new GClass(this, classDecl, null);
    }

    public GClass createOrGetAnonymousClass(String tag) {
        String className = ANONYMOUS_CLASS_PREFIX + tag;
        GClass gClass = getGClass(className);
        if (gClass == null) {
            gClass = createAnonymousClass();
            gClass.setName(className);
            putGClass(gClass);
        }
        return gClass;
    }

    public void addNewImports(List<JCImport> newImports) {
        List<JCTree> defs = List.nil();
        defs = defs.appendList(List.convert(JCTree.class, mergeImports(compilationUnit.getImports(), newImports)));
        defs = defs.appendList(compilationUnit.getTypeDecls());
        compilationUnit.defs = defs;
    }

    public void addNewImports(String... imports) {
        TreeMaker maker = environment.getMaker();
        List<JCImport> newImports = List.nil();
        for (String newImport : imports) {
            newImports = newImports.append(maker.Import(environment.createParser(newImport).parseType(), false));
        }

        addNewImports(newImports);
    }

    public String getName() {
        for (JCTree type : compilationUnit.getTypeDecls()) {
            if (type instanceof JCClassDecl && ((JCClassDecl) type).getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                return ((JCClassDecl) type).getSimpleName().toString();
            }
        }
        return null;
    }
}
