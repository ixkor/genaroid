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

package net.xkor.genaroid;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
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
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;

import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GElement;
import net.xkor.genaroid.tree.GField;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.tree.GUnit;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class GenaroidEnvironment {
    public static final String GENAROID_MAIN_CLASS = "net.xkor.genaroid.Genaroid";
    public static final String DEBUG_MODE_OPTION_NAME = "genaroidDebugMode";
    public static final String SAVE_TEMPLATES_OPTION_NAME = "saveTemplates";
    public static final String PROJECT_PATH_OPTION_NAME = "projectPath";
    private static final Pattern S_OPTION_MATCHER = Pattern.compile("^(.*)[/\\\\]build[/\\\\]generated[/\\\\]source[/\\\\]apt[/\\\\](\\w+)$");
    private JavacProcessingEnvironment javacProcessingEnv;
    private RoundEnvironment roundEnvironment;
    private TreeMaker maker;
    private ParserFactory parserFactory;
    private Method newParserMethod;
    private JavacElements utils;
    private JavacTrees trees;
    private JavacTypes typeUtils;
    private Types types;
    private JavaCompiler compiler;
    private JavacFileManager javacFileManager;

    private JCExpression voidType;
    private Symbol.ClassSymbol objectClass;

    private HashMap<String, GUnit> units = new HashMap<>();
    private java.util.List<GClass> fragments;
    private java.util.List<GClass> activities;
    private boolean debugMode;
    private boolean saveTemplates;
    private String projectPath;
    private String buildVariant;

    public void init(ProcessingEnvironment procEnv) {
        javacProcessingEnv = (JavacProcessingEnvironment) procEnv;
        maker = TreeMaker.instance(javacProcessingEnv.getContext());
        parserFactory = ParserFactory.instance(javacProcessingEnv.getContext());
        utils = javacProcessingEnv.getElementUtils();
        typeUtils = javacProcessingEnv.getTypeUtils();
        trees = JavacTrees.instance(javacProcessingEnv);
        types = Types.instance(javacProcessingEnv.getContext());
        compiler = JavaCompiler.instance(javacProcessingEnv.getContext());
        javacFileManager = (JavacFileManager) javacProcessingEnv.getContext().get(JavaFileManager.class);

        voidType = maker.Type((Type) typeUtils.getNoType(TypeKind.VOID));
        objectClass = utils.getTypeElement("java.lang.Object");

        debugMode = Boolean.parseBoolean(javacProcessingEnv.getOptions().get(DEBUG_MODE_OPTION_NAME));
        saveTemplates = Boolean.parseBoolean(javacProcessingEnv.getOptions().get(SAVE_TEMPLATES_OPTION_NAME));
        projectPath = javacProcessingEnv.getOptions().get(PROJECT_PATH_OPTION_NAME);
        String s = Options.instance(javacProcessingEnv.getContext()).get("-s");
        if (s != null && s.length() != 0) {
            Matcher matcher = S_OPTION_MATCHER.matcher(s);
            if (matcher.find()) {
                if (projectPath == null || projectPath.length() == 0) {
                    projectPath = matcher.group(1);
                }
                buildVariant = matcher.group(2);
            }
        }

        // reflection
        try {
            newParserMethod = ParserFactory.class.getMethod("newParser", CharSequence.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE);
        } catch (NoSuchMethodException ignored) {
        }
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

    public <T extends GElement> Set<T> getGElementsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<T> elementClass) {
        return getGElementsAnnotatedWith(roundEnvironment.getElementsAnnotatedWith(annotationClass), elementClass);
    }

    public <T extends GElement> Set<T> getGElementsAnnotatedWith(Symbol.ClassSymbol annotationClass, Class<T> elementClass) {
        return getGElementsAnnotatedWith(roundEnvironment.getElementsAnnotatedWith(annotationClass), elementClass);
    }

    private <T extends GElement> Set<T> getGElementsAnnotatedWith(Set<? extends Element> elements, Class<T> elementClass) {
        Set<T> result = new HashSet<>();
        for (Element element : elements) {
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
                result.add(elementClass.cast(gElement));
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

    public <T extends Annotation> void removeAnnotation(JCModifiers modifiers, T annotation) {
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

    public TreeMaker getMaker() {
        return maker;
    }

    public JavacElements getUtils() {
        return utils;
    }

    public JCExpression getVoidType() {
        return voidType;
    }

    public JCExpression typeToTree(Type type) {
        return typeToTree(type.asElement());
    }

    public JCExpression typeToTree(Symbol.TypeSymbol typeSymbol) {
        return createParser(typeSymbol.getQualifiedName().toString()).parseType();
    }

    public Parser createParser(String sources) {
        try {
            return (Parser) newParserMethod.invoke(parserFactory, sources, false, false, false);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        return null;
    }

    public JCTree.JCStatement codeToStatement(String code, Object... args) {
        return createParser(String.format(code, args)).parseStatement();
    }

    public JCTree.JCExpression codeToExpression(String code, Object... args) {
        return createParser(String.format(code, args)).parseExpression();
    }

    public JCCompilationUnit parseUnit(String source) {
        return compiler.parse(new MemoryJavaFileObject(source));
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

    public Collection<GClass> getClasses() {
        ArrayList<GClass> classes = new ArrayList<>();
        for (GUnit unit : units.values()) {
            classes.addAll(unit.getGClasses());
        }
        return classes;
    }

    public Messager getMessager() {
        return javacProcessingEnv.getMessager();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public JavacProcessingEnvironment getJavacProcessingEnv() {
        return javacProcessingEnv;
    }

    public Symbol.ClassSymbol getObjectClass() {
        return objectClass;
    }

    public java.util.List<GClass> getFragments() {
        return fragments;
    }

    public void setFragments(java.util.List<GClass> fragments) {
        this.fragments = fragments;
    }

    public java.util.List<GClass> getActivities() {
        return activities;
    }

    public void setActivities(java.util.List<GClass> activities) {
        this.activities = activities;
    }

    public boolean isSaveTemplates() {
        return saveTemplates;
    }

    public String getBuildVariant() {
        return buildVariant;
    }

    public String getProjectPath() {
        return projectPath;
    }

    private class MemoryJavaFileObject implements JavaFileObject {
        private String source;

        public MemoryJavaFileObject(String source) {
            this.source = source;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            return false;
        }

        @Override
        public NestingKind getNestingKind() {
            return null;
        }

        @Override
        public Modifier getAccessLevel() {
            return null;
        }

        @Override
        public URI toUri() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            CharSequence charContent = getCharContent(ignoreEncodingErrors);
            if (charContent == null)
                throw new UnsupportedOperationException();
            if (charContent instanceof CharBuffer) {
                CharBuffer buffer = (CharBuffer) charContent;
                if (buffer.hasArray())
                    return new CharArrayReader(buffer.array());
            }
            return new StringReader(charContent.toString());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return source;
        }

        @Override
        public Writer openWriter() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public boolean delete() {
            return false;
        }
    }
}
