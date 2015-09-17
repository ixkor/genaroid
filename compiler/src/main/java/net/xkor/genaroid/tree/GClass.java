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
package net.xkor.genaroid.tree;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Filter;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.lang.model.element.Element;

public class GClass extends GElement {
    private static final Filter<Symbol> methodsFilter = new Filter<Symbol>() {
        public boolean accepts(Symbol symbol) {
            return symbol instanceof Symbol.MethodSymbol;
        }
    };
    private GUnit unit;
    private JCClassDecl classDecl;
    private HashMap<String, GClassMember> members = new HashMap<>();

    public GClass(GUnit unit, JCClassDecl classDecl, Element element) {
        super(element);
        this.unit = unit;
        this.classDecl = classDecl;
    }

    public static GClass getGClass(GenaroidEnvironment environment, Element element) {
        JCClassDecl classDecl = (JCClassDecl) environment.getUtils().getTree(element);
        GUnit unit = GUnit.getGUnit(environment, element);

        String className = GElement.getName(element);
        GClass gClass = unit.getGClass(className);
        if (gClass == null) {
            gClass = new GClass(unit, classDecl, element);
            unit.putGClass(gClass);
        }
        return gClass;
    }

    public JCClassDecl getClassDecl() {
        return classDecl;
    }

    @Override
    public Symbol.ClassSymbol getElement() {
        return (Symbol.ClassSymbol) super.getElement();
    }

    @Override
    public GUnit getGUnit() {
        return unit;
    }

    @Override
    public JCClassDecl getTree() {
        return classDecl;
    }

    @Override
    protected JCModifiers getModifiers() {
        return getTree().getModifiers();
    }

    public Collection<GClassMember> getMembers() {
        return members.values();
    }

    public GClassMember getMember(String memberSignature) {
        return members.get(memberSignature);
    }

    public void putMember(GClassMember member) {
        members.put(member.getMemberSignature(), member);
    }

    public boolean isSubClass(Symbol.ClassSymbol base) {
        return getElement().isSubClass(base, getEnvironment().getTypes());
    }

    public boolean isSubClass(GClass base) {
        return isSubClass(base.getElement());
    }

    public <T extends GClassMember> void implementInBestParent(Symbol.ClassSymbol interfaceType, Set<T> members) {
        if (!isSubClass(interfaceType)) {
            GClass classToRestorableImplement = this;
            for (GClassMember member : members) {
                if (member.getGClass() != classToRestorableImplement && classToRestorableImplement.isSubClass(member.getGClass())) {
                    classToRestorableImplement = member.getGClass();
                }
            }
            classToRestorableImplement.implement(interfaceType);
        }
    }

    public void implement(Symbol.ClassSymbol interfaceType) {
        if (!interfaceType.isInterface()) {
            throw new RuntimeException("Can not implement non interface type");
        }
        if (isImplementedByProcessor(interfaceType)) {
            return;
        }

        JCExpression jcInterface = getEnvironment().createParser(interfaceType.getQualifiedName().toString()).parseType();
        classDecl.implementing = classDecl.implementing.append(jcInterface);

        do {
            for (Symbol symbol : interfaceType.members().getElements(methodsFilter)) {
                overrideMethod((Symbol.MethodSymbol) symbol, false);
            }
            interfaceType = (Symbol.ClassSymbol) interfaceType.getSuperclass().asElement();
        } while (interfaceType != null);
    }

    public boolean isImplementedByProcessor(Symbol.ClassSymbol interfaceType) {
        String interfaceName = interfaceType.getQualifiedName().toString();
        for (JCExpression expression : classDecl.implementing) {
            if (expression.toString().equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }

//    public GMethod createOrGetMethod(long modifiers, String name, String type, List<JCVariableDecl> params) {
//        TreeMaker maker = getEnvironment().getMaker();
//        JavacElements utils = getEnvironment().getUtils();
//        JCExpression returnType = getEnvironment().getVoidType();
//        if (type != null) {
//            returnType = getEnvironment().createParser(type).parseType();
//        }
//        JCMethodDecl methodDecl = maker.MethodDef(
//                maker.Modifiers(modifiers),
//                utils.getName(name),
//                returnType,
//                List.<JCTypeParameter>nil(),
//                params,
//                List.<JCExpression>nil(),
//                maker.Block(0, List.<JCStatement>nil()),
//                null);
//
//        GMethod method = (GMethod) getMember(memberSignature);
//        if (method == null) {
//            method = new GMethod(this, methodDecl, null);
//            method.setName(name);
//            putMember(method);
//            classDecl.defs = classDecl.defs.append(methodDecl);
//        }
//
//        return method;
//    }

    public GField createOrGetField(long modifiers, String name, String type, JCTree.JCExpression init) {
        GField field = (GField) getMember(name);
        if (field == null) {
            TreeMaker maker = getEnvironment().getMaker();
            JavacElements utils = getEnvironment().getUtils();
            JCVariableDecl variableDecl = maker.VarDef(maker.Modifiers(modifiers), utils.getName(name),
                    maker.Ident(utils.getName(type)), init);

            field = new GField(this, variableDecl, null);
            field.setName(name);
            field.setMemberSignature(name);
            putMember(field);
            classDecl.defs = classDecl.defs.append(variableDecl);
        }

        return field;
    }

    public GMethod overrideMethod(Symbol.MethodSymbol methodSymbol, boolean addSupperCall) {
        String memberSignature = GClassMember.getMemberSignature(methodSymbol);
        GMethod method = (GMethod) getMember(memberSignature);
        if (method != null) {
            return method;
        }

        for (Element member : getElement().getEnclosedElements()) {
            String signature = GClassMember.getMemberSignature(member);
            if (member instanceof Symbol.MethodSymbol && signature.equals(memberSignature)) {
                return GMethod.getGMethod(getEnvironment(), member);
            }
        }

        TreeMaker maker = getEnvironment().getMaker();
        JavacElements utils = getEnvironment().getUtils();

        String typeName = methodSymbol.getReturnType().asElement().getQualifiedName().toString();
        JCExpression returnType = getEnvironment().createParser(typeName).parseType();
        List<JCVariableDecl> params = List.nil();
        int paramNum = 0;
        for (Type paramType : methodSymbol.asType().getParameterTypes()) {
            Name paramName = utils.getName("param" + paramNum++);
            JCExpression returnTypeName = getEnvironment().createParser(paramType.asElement().getQualifiedName().toString()).parseType();
            params = params.append(maker.VarDef(maker.Modifiers(0), paramName, returnTypeName, null));
        }
        long modifiers = methodSymbol.flags() & (Flags.PUBLIC | Flags.PRIVATE | Flags.PROTECTED);
        JCTree.JCAnnotation overrideAnnotation = maker.Annotation(maker.Ident(utils.getName("Override")), List.<JCExpression>nil());

        JCMethodDecl methodDecl = maker.MethodDef(
                maker.Modifiers(modifiers, List.of(overrideAnnotation)),
                utils.getName(methodSymbol.getSimpleName()),
                returnType,
                List.<JCTypeParameter>nil(),
                params,
                List.<JCExpression>nil(),
                maker.Block(0, List.<JCStatement>nil()),
                null);

        method = new GMethod(this, methodDecl, null);
        method.setName(methodSymbol.getSimpleName().toString());
        method.setMemberSignature(memberSignature);

        if (addSupperCall) {
            StringBuilder superCallSource = new StringBuilder("super.");
            superCallSource.append(methodSymbol.getSimpleName().toString());
            superCallSource.append("(");

            for (JCVariableDecl param : methodDecl.getParameters()) {
                if (superCallSource.charAt(superCallSource.length() - 1) != '(') {
                    superCallSource.append(", ");
                }
                superCallSource.append(param.getName());
            }
            superCallSource.append(");");

            JCStatement superCall = getEnvironment().createParser(superCallSource.toString()).parseStatement();
            method.appendCode(superCall);
        }

        putMember(method);
        classDecl.defs = classDecl.defs.append(methodDecl);

        return method;
    }

    public JCNewClass createNewInstance(String className, List<JCExpression> params) {
        TreeMaker maker = getEnvironment().getMaker();
        JavacElements utils = getEnvironment().getUtils();
        JCIdent ident = maker.Ident(utils.getName(className));
        return maker.NewClass(null, null, ident, params, getTree());
    }

    public JCNewClass createNewInstance(List<JCExpression> params) {
        return createNewInstance(getName(), params);
    }
}
