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
package net.xkor.genaroid;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GElement;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.tree.GUnit;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

public class GenaroidEnvironment {
    private JavacProcessingEnvironment javacProcessingEnv;
    private RoundEnvironment roundEnvironment;
    private TreeMaker maker;
    private ParserFactory parserFactory;
    private JavacElements utils;
    private JavacTrees trees;
    private JavacTypes typeUtils;
    private Types types;

    private JCExpression voidType;

    private HashMap<String, GUnit> units = new HashMap<>();

    public void init(ProcessingEnvironment procEnv) {
        javacProcessingEnv = (JavacProcessingEnvironment) procEnv;
        maker = TreeMaker.instance(javacProcessingEnv.getContext());
        parserFactory = ParserFactory.instance(javacProcessingEnv.getContext());
        utils = javacProcessingEnv.getElementUtils();
        typeUtils = javacProcessingEnv.getTypeUtils();
        trees = JavacTrees.instance(javacProcessingEnv);
        types = Types.instance(javacProcessingEnv.getContext());

        voidType = maker.Type((Type) typeUtils.getNoType(TypeKind.VOID));
    }

    public Pair<JCTree, JCCompilationUnit> getTreeAndTopLevel(Element e) {
        return utils.getTreeAndTopLevel(e, null, null);
    }

    public RoundEnvironment getRoundEnvironment() {
        return roundEnvironment;
    }

    public void setRoundEnvironment(RoundEnvironment roundEnvironment) {
        this.roundEnvironment = roundEnvironment;
    }

    public <T extends GElement> Set<T> getGElementsAnnotatedWith(Symbol.ClassSymbol annotationClass, Class<T> elementClass) {
        Set<T> result = new HashSet<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(annotationClass)) {
            GElement gElement = null;
            switch (element.getKind()) {
                case CLASS:
                    gElement = GClass.getGClass(this, element);
                    break;
                case FIELD:
                    gElement = GField.getGField(this, element);
                    break;
                case METHOD:
                    gElement = GMethod.getGMethod(this, element);
                    break;
            }
            if (gElement != null && elementClass.isAssignableFrom(gElement.getClass())) {
                //noinspection unchecked
                result.add((T) gElement);
            }
        }
        return result;
    }

    public void removeAnnotation(JCModifiers modifiers, JCAnnotation annotation) {
        ArrayList<JCAnnotation> elementAnnotations = new ArrayList<>(modifiers.annotations);
        elementAnnotations.remove(annotation);
        modifiers.annotations = List.from(
                elementAnnotations.toArray(new JCAnnotation[elementAnnotations.size()]));
    }

    public JCAnnotation findAnnotation(JCModifiers modifiers, Symbol.ClassSymbol annotationClass) {
        for (JCAnnotation annotation : modifiers.annotations) {
            if (equalAnnotation(annotation, annotationClass)) {
                return annotation;
            }
        }
        return null;
    }

    public boolean equalAnnotation(JCAnnotation jcAnnotation, Symbol.ClassSymbol annotationClass) {
        String name = jcAnnotation.getAnnotationType().toString();
        return name.equals(annotationClass.className()) || name.equals(annotationClass.getSimpleName().toString());
    }

//    public Element findElement(JCCompilationUnit compilationUnit, JCTree tree) {
//        return trees.getElement(trees.getPath(compilationUnit, tree));
//    }

    public TreeMaker getMaker() {
        return maker;
    }

    public JavacElements getUtils() {
        return utils;
    }

    public JCExpression getVoidType() {
        return voidType;
    }

    public Parser createParser(String sources) {
        return parserFactory.newParser(sources, false, false, false);
    }

    public JavacTypes getTypeUtils() {
        return typeUtils;
    }

    public Types getTypes() {
        return types;
    }

    public GUnit getUnit(String sourceFile) {
        return units.get(sourceFile);
    }

    public void putUnit(GUnit unit) {
        units.put(unit.getCompilationUnit().getSourceFile().getName(), unit);
    }

    public Collection<GUnit> getUnits() {
        return units.values();
    }

    //    public void test() {
//        Symbol.ClassSymbol class_AppCompatActivity = utils.getTypeElement("android.support.v7.app.AppCompatActivity");
//        GClass clazz = getGClass(class_AppCompatActivity);
//    }

    public Messager getMessager(){
        return javacProcessingEnv.getMessager();
    }
}
