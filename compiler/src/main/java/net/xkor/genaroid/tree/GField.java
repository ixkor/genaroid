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

import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import net.xkor.genaroid.GenaroidEnvironment;

import javax.lang.model.element.Element;

public class GField extends GClassMember {
    private JCVariableDecl variableDecl;

    public GField(GClass gClass, JCVariableDecl variableDecl, Element element) {
        super(gClass, element);
        this.variableDecl = variableDecl;
    }

    @Override
    public JCVariableDecl getTree() {
        return variableDecl;
    }

    @Override
    protected JCModifiers getModifiers() {
        return getTree().getModifiers();
    }

    public static GField getGField(GenaroidEnvironment environment, Element element) {
        GClass gClass = GClass.getGClass(environment, element.getEnclosingElement());
        JCVariableDecl variableDecl = (JCVariableDecl) environment.getUtils().getTree(element);

        String memberName = GClassMember.getMemberSignature(element);
        GField field = (GField) gClass.getMember(memberName);
        if (field == null) {
            field = new GField(gClass, variableDecl, element);
            gClass.putMember(field);
        }

        return field;
    }
}
