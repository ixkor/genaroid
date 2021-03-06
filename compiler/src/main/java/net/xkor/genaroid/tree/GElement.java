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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;

import net.xkor.genaroid.GenaroidEnvironment;

import java.lang.annotation.Annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public abstract class GElement {
    private Element element;
    private String name;

    public GElement(Element element) {
        this.element = element;
    }

    public static String getName(Element element) {
        if (element == null) {
            return null;
        }
        return element.getSimpleName().toString();
    }

    public Element getElement() {
        return element;
    }

    public String getName() {
        if (name == null) {
            name = getName(element);
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected GenaroidEnvironment getEnvironment() {
        return getGUnit().getEnvironment();
    }

    public abstract GUnit getGUnit();

    public abstract JCTree getTree();

    protected abstract JCModifiers getModifiers();

    public JCAnnotation getAnnotation(Symbol.ClassSymbol annotationClass) {
        return getEnvironment().findAnnotation(getModifiers(), annotationClass);
    }

    public void removeAnnotation(JCAnnotation annotation) {
        getEnvironment().removeAnnotation(getModifiers(), annotation);
    }

    public JCAnnotation extractAnnotation(Symbol.ClassSymbol annotationClass) {
        JCAnnotation annotation = getAnnotation(annotationClass);
        if (annotation != null) {
            removeAnnotation(annotation);
        }
        return annotation;
    }

    public AnnotationMirror findAnnotationMirror(Type annotationType) {
        for (AnnotationMirror mirror : getElement().getAnnotationMirrors()) {
            if (mirror.getAnnotationType() == annotationType) {
                return mirror;
            }
        }
        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return getElement().getAnnotation(annotationClass);
    }
}
