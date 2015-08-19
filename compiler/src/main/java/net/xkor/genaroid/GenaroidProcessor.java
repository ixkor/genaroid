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

import com.sun.tools.javac.util.List;

import net.xkor.genaroid.processing.InstanceStateProcessor;
import net.xkor.genaroid.processing.SubProcessor;
import net.xkor.genaroid.processing.ViewByIdProcessor;
import net.xkor.genaroid.tree.GUnit;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class GenaroidProcessor extends AbstractProcessor {
    private int counter = 0;
    private GenaroidEnvironment genaroidEnvironment = new GenaroidEnvironment();
    private ArrayList<SubProcessor> processors = new ArrayList<>();

    public GenaroidProcessor() {
        processors.clear();
        processors.add(new ViewByIdProcessor());
        processors.add(new InstanceStateProcessor());
    }

    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);
        genaroidEnvironment.init(procEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty() || counter > 0) {
            return false;
        }
        counter++;
        genaroidEnvironment.setRoundEnvironment(roundEnv);

        try {
            for (SubProcessor processor : processors) {
                processor.process(genaroidEnvironment);
            }

            for (GUnit unit : genaroidEnvironment.getUnits()) {
                try {
                    JavaFileObject source = processingEnv.getFiler().createSourceFile(
                            unit.getCompilationUnit().getPackageName() + "." + unit.getName());
                    Writer writer = source.openWriter();
                    writer.write(unit.getCompilationUnit().toString());
                    writer.flush();
                    writer.close();
                    unit.getCompilationUnit().defs = List.nil();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void logDebug(String message) {
        System.out.println(message);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        for (SubProcessor processor : processors) {
            types.addAll(processor.getSupportedAnnotationTypes());
        }
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
