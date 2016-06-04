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

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Filter;
import com.sun.tools.javac.util.List;

import net.xkor.genaroid.annotations.CustomListener;
import net.xkor.genaroid.tree.GClass;
import net.xkor.genaroid.tree.GMethod;
import net.xkor.genaroid.wrap.BindableWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

@AutoService(GenaroidPlugin.class)
@GenaroidPlugin.Dependencies(ViewByIdPlugin.class)
public class ListenersPlugin extends GenaroidPlugin {
    private static final String ANNOTATION_CLASS_NAME = CustomListener.class.getCanonicalName();
    private static final String[] STANDARD_LISTENER_ANNOTATIONS = new String[]{
            "net.xkor.genaroid.annotations.OnClick",
            "net.xkor.genaroid.annotations.OnItemClick",
            "net.xkor.genaroid.annotations.OnLongClick",
            "net.xkor.genaroid.annotations.OnItemLongClick",
            "net.xkor.genaroid.annotations.OnItemSelected",
            "net.xkor.genaroid.annotations.OnNothingSelected",
    };

    @Override
    public void process() {
        JavacElements utils = getEnvironment().getUtils();
        Types types = getEnvironment().getTypes();
        TreeMaker maker = getEnvironment().getMaker();
        BindableWrapper bindableWrapper = new BindableWrapper(utils);
        PrimitiveType intType = getEnvironment().getTypeUtils().getPrimitiveType(TypeKind.INT);

        HashMap<String, GClass> listeners = new HashMap<>();
        Collection<Symbol.ClassSymbol> annotations = getListenerAnnotations();
        for (Symbol.ClassSymbol annotationSymbol : annotations) {
            CustomListener customListener = ((Element) annotationSymbol).getAnnotation(CustomListener.class);
            Target target = ((Element) annotationSymbol).getAnnotation(Target.class);
            boolean validAnnotation = true;
            if (target == null || target.value().length != 1 || target.value()[0] != ElementType.METHOD) {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Annotation " + annotationSymbol.getSimpleName() + " must be annotated by @Target(ElementType.METHOD)",
                        annotationSymbol);
                validAnnotation = false;
            }
            for (Symbol member : annotationSymbol.members().getElements()) {
                if (member instanceof Symbol.MethodSymbol) {
                    if (!"value".equals(member.name.toString())) {
                        getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Annotation " + annotationSymbol.getSimpleName() + " must has only value parameter",
                                annotationSymbol);
                        validAnnotation = false;
                    }

                    Type returnType = member.type.asMethodType().getReturnType();
                    if (!types.isArray(returnType) || !types.elemtype(returnType).equals(intType)) {
                        getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Annotation " + annotationSymbol.getSimpleName() + " must has value parameter with int[] type",
                                annotationSymbol);
                        validAnnotation = false;
                    }
                }
            }

            Symbol.ClassSymbol listenerClassSymbol;
            try {
                Class<?> clazz = customListener.listenerClass();
                listenerClassSymbol = utils.getTypeElement(clazz.getCanonicalName());
            } catch (MirroredTypeException exception) {
                DeclaredType classTypeMirror = (DeclaredType) exception.getTypeMirror();
                listenerClassSymbol = (Symbol.ClassSymbol) classTypeMirror.asElement();
            }

            Symbol.ClassSymbol targetClassSymbol;
            try {
                Class<?> clazz = customListener.targetClass();
                targetClassSymbol = utils.getTypeElement(clazz.getCanonicalName());
            } catch (MirroredTypeException exception) {
                DeclaredType classTypeMirror = (DeclaredType) exception.getTypeMirror();
                targetClassSymbol = (Symbol.ClassSymbol) classTypeMirror.asElement();
            }

            Symbol.MethodSymbol listenerSetter = null;
            for (Symbol member : targetClassSymbol.members().getElementsByName(utils.getName(customListener.listenerSetterName()))) {
                if (member instanceof Symbol.MethodSymbol) {
                    Symbol.MethodSymbol method = (Symbol.MethodSymbol) member;
                    List<Type> params = method.asType().asMethodType().getParameterTypes();
                    if (params.size() == 1 && params.get(0).asElement() == listenerClassSymbol) {
                        listenerSetter = method;
                    }
                }
            }
            if (listenerSetter == null) {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("Can not found method %s(%s) in class %s", customListener.listenerSetterName(), listenerClassSymbol.getQualifiedName(), targetClassSymbol.getQualifiedName()),
                        annotationSymbol);
                validAnnotation = false;
            }

            Symbol.MethodSymbol listenerMethod = null;
            ListenerMethodFilter listenerMethodFilter = new ListenerMethodFilter(customListener.listenerMethodName());
            for (Symbol member : listenerClassSymbol.members().getElements(listenerMethodFilter)) {
                if (listenerMethod != null) {
                    getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                            String.format("Listener class %s contains more one method, please specify method by a listenerSetterName parameter", listenerClassSymbol.getQualifiedName()),
                            annotationSymbol);
                    validAnnotation = false;
                }
                listenerMethod = (Symbol.MethodSymbol) member;
            }
            if (listenerMethod == null) {
                getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("Listener class %s doesn't contains one method %s", listenerClassSymbol.getQualifiedName(), customListener.listenerSetterName()),
                        annotationSymbol);
                validAnnotation = false;
            }

            if (!validAnnotation) {
                continue;
            }

            Set<GMethod> methods = getEnvironment().getGElementsAnnotatedWith(annotationSymbol, GMethod.class);

            for (GMethod method : methods) {
                JCTree.JCAnnotation annotation = method.extractAnnotation(annotationSymbol);
                JCTree.JCExpression value = annotation.getArguments().get(0);
                Set<String> viewIds = new HashSet<>();
                if (value instanceof JCTree.JCAssign) {
                    value = ((JCTree.JCAssign) value).rhs;
                }
                if (value instanceof JCTree.JCNewArray) {
                    List<JCTree.JCExpression> elems = ((JCTree.JCNewArray) value).elems;
                    for (JCTree.JCExpression elem : elems) {
                        viewIds.add(elem.toString());
                    }
                } else {
                    viewIds.add(value.toString());
                }

                maker.toplevel = method.getGClass().getGUnit().getCompilationUnit();
                method.getGClass().implementIfNeeded(bindableWrapper.getClassSymbol());
                for (String viewId : viewIds) {
                    String listenerVarName = "listener" + Integer.toHexString((method.getGClass().getElement().getQualifiedName() + "_" + viewId + "_" + listenerSetter.getQualifiedName() + "_" + listenerClassSymbol.getQualifiedName()).hashCode());
                    GClass listenerImplementor = listeners.get(listenerVarName);
                    if (listenerImplementor == null) {
                        listenerImplementor = method.getGClass().getGUnit().createAnonymousClass(method.getGClass().getClassDecl());
                        listenerImplementor.implement(listenerClassSymbol);
                        listeners.put(listenerVarName, listenerImplementor);
                        JCTree.JCExpression listenerType = getEnvironment().typeToTree(listenerClassSymbol);
                        JCTree.JCVariableDecl listenerStatement = maker.VarDef(maker.Modifiers(0),
                                utils.getName(listenerVarName), listenerType,
                                maker.NewClass(null, null, listenerType, List.<JCTree.JCExpression>nil(), listenerImplementor.getTree()));
                        method.getGClass().overrideMethod(bindableWrapper.getBindMethod(), true)
                                .appendCode(listenerStatement)
                                .appendCode("((%s) $p0.findViewById(%s)).%s(%s);", targetClassSymbol.getQualifiedName(), viewId, listenerSetter.getSimpleName(), listenerVarName);
                    }
                    GMethod listenerOverridedMethod = listenerImplementor.overrideMethod(listenerMethod, false);
                    List<Symbol.VarSymbol> listenerParameters = listenerMethod.getParameters();
                    List<JCTree.JCExpression> paramList = List.nil();
                    int paramNum = 0;
                    boolean allParamsFound = true;
                    for (Symbol.VarSymbol param : method.getElement().getParameters()) {
                        boolean found = false;
                        for (; paramNum < listenerParameters.size(); paramNum++) {
                            if (types.isSameType(listenerParameters.get(paramNum).asType(), param.asType())) {
                                paramList = paramList.append(maker.Ident(listenerOverridedMethod.getParamName(paramNum)));
                                found = true;
                                paramNum++;
                                break;
                            }
                        }
                        allParamsFound &= found;
                        if (!found) {
                            getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Can not found listener parameter with type " + param.asType().toString(),
                                    method.getElement());
                        }
                    }
                    if (allParamsFound) {
                        listenerOverridedMethod.appendCode(maker.Exec(maker.Apply(null, maker.Ident(method.getElement()), paramList)));
                    }
                }
            }
        }

        for (GClass gClass : getEnvironment().getClasses()) {
            gClass.fixImplementation(bindableWrapper.getClassSymbol());
        }
    }

    private HashSet<Symbol.ClassSymbol> getListenerAnnotations() {
        HashSet<Symbol.ClassSymbol> annotations = new HashSet<>();
        for (String annotationName : STANDARD_LISTENER_ANNOTATIONS) {
            annotations.add(getEnvironment().getUtils().getTypeElement(annotationName));
        }
        for (Element element : getEnvironment().getRoundEnvironment().getElementsAnnotatedWith(CustomListener.class)) {
            annotations.add((Symbol.ClassSymbol) element);
        }
        return annotations;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION_CLASS_NAME);
    }

    private static class ListenerMethodFilter implements Filter<Symbol> {
        private final String listenerMethodName;

        public ListenerMethodFilter(String listenerMethodName) {
            this.listenerMethodName = listenerMethodName;
        }

        @Override
        public boolean accepts(Symbol symbol) {
            return (symbol instanceof Symbol.MethodSymbol) && (listenerMethodName.length() == 0 || listenerMethodName.equals(symbol.name.toString()));
        }
    }
}
