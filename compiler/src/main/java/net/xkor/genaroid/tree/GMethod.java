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

    @Override
    public JCMethodDecl getTree() {
        return methodDecl;
    }

    @Override
    protected JCModifiers getModifiers() {
        return getTree().getModifiers();
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

    public Name getParamName(int index) {
        return methodDecl.getParameters().get(index).getName();
    }

    public void appendCode(JCStatement code) {
        methodDecl.getBody().stats = methodDecl.getBody().stats.append(code);
    }

    public void prependCode(JCStatement code) {
        methodDecl.getBody().stats = methodDecl.getBody().stats.prepend(code);
    }

    public List<JCStatement> getBody() {
        return methodDecl.getBody().stats;
    }

    public void setBody(List<JCStatement> body) {
        methodDecl.getBody().stats = body;
    }
}
