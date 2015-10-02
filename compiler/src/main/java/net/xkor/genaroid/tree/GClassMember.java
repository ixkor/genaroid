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

import javax.lang.model.element.Element;

public abstract class GClassMember extends GElement {
    private GClass gClass;
    private String signature;

    public GClassMember(GClass gClass, Element element) {
        super(element);
        this.gClass = gClass;
    }

    public static String getMemberSignature(Element element) {
        if (element == null) {
            return null;
        }
        return element.toString();
    }

    public GClass getGClass() {
        return gClass;
    }

    @Override
    public GUnit getGUnit() {
        return gClass.getGUnit();
    }

    public String getMemberSignature(){
        if (signature == null) {
            signature = getMemberSignature(getElement());
        }
        return signature;
    }

    public void setMemberSignature(String signature) {
        this.signature = signature;
    }

}
