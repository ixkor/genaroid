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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import net.xkor.genaroid.GenaroidEnvironment;

import javax.lang.model.element.Element;

public class GMethod extends GClassMember {
    JCMethodDecl methodDecl;

    public GMethod(GClass gClass, JCMethodDecl methodDecl, Element element) {
        super(gClass, element);
        this.methodDecl = methodDecl;
    }

    public static GMethod getGMethod(GenaroidEnvironment environment, Element element) {
        GClass gClass = GClass.getGClass(environment, element.getEnclosingElement());
        JCMethodDecl methodDecl = (JCMethodDecl) environment.getUtils().getTree(element);

        String memberName = GClassMember.getMemberSignature(element);
        GMethod method = (GMethod) gClass.getMember(memberName);
        if (method == null) {
            method = new GMethod(gClass, methodDecl, element);
            gClass.putMember(method);
        }

        return method;
    }

    @Override
    public JCMethodDecl getTree() {
        return methodDecl;
    }

    @Override
    public Symbol.MethodSymbol getElement() {
        return (Symbol.MethodSymbol) super.getElement();
    }

    @Override
    protected JCModifiers getModifiers() {
        return getTree().getModifiers();
    }

    public Name getParamName(int index) {
        return methodDecl.getParameters().get(index).getName();
    }

    public GMethod appendCode(JCStatement code) {
        methodDecl.getBody().stats = methodDecl.getBody().stats.append(code);
        return this;
    }

    public void appendCodeAfterSuper(JCStatement code) {
        String superCall = "super." + getName() + "(";
        List<JCStatement> newBody = List.nil();
        boolean added = false;
        for (JCStatement statement : methodDecl.getBody().stats) {
            newBody = newBody.append(statement);
            if (!added && statement.toString().startsWith(superCall)) {
                newBody = newBody.append(code);
                added = true;
            }
        }
        methodDecl.getBody().stats = newBody;
    }

    public GMethod appendCodeAfterSuper(String code, Object... args) {
        appendCodeAfterSuper(codeToStatement(code, args));
        return this;
    }

    public GMethod appendCode(String code, Object... args) {
        appendCode(codeToStatement(code, args));
        return this;
    }

    private JCStatement codeToStatement(String code, Object[] args) {
        for (int i = 0; i < methodDecl.getParameters().size(); i++) {
            code = code.replace("$p" + i, getParamName(i));
        }
        return getEnvironment().createParser(String.format(code, args)).parseStatement();
    }

    public GMethod prependCode(JCStatement code) {
        methodDecl.getBody().stats = methodDecl.getBody().stats.prepend(code);
        return this;
    }

    public GMethod prependCode(String code, Object... args) {
        prependCode(codeToStatement(code, args));
        return this;
    }

    public List<JCStatement> getBody() {
        return methodDecl.getBody().stats;
    }

    public void setBody(List<JCStatement> body) {
        methodDecl.getBody().stats = body;
    }

    public GMethod appendSuperCall() {
        StringBuilder superCallSource = new StringBuilder("super.");
        superCallSource.append(getName());
        superCallSource.append("(");

        for (JCTree.JCVariableDecl param : methodDecl.getParameters()) {
            if (superCallSource.charAt(superCallSource.length() - 1) != '(') {
                superCallSource.append(", ");
            }
            superCallSource.append(param.getName());
        }
        superCallSource.append(");");

        appendCode(superCallSource.toString());
        return this;
    }
}
