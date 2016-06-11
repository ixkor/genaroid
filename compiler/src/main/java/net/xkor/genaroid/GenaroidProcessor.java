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

import com.google.auto.service.AutoService;
import com.sun.tools.javac.util.List;

import net.xkor.genaroid.plugins.GenaroidPlugin;
import net.xkor.genaroid.plugins.PluginsManager;
import net.xkor.genaroid.tree.GUnit;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class GenaroidProcessor extends AbstractProcessor {
    private int counter = 0;
    private GenaroidEnvironment genaroidEnvironment = new GenaroidEnvironment();
    private PluginsManager pluginsManager;

    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);
        genaroidEnvironment.init(procEnv);
        pluginsManager = new PluginsManager(genaroidEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty() || counter > 0) {
            return false;
        }
        counter++;
        genaroidEnvironment.setRoundEnvironment(roundEnv);

        try {
            long startTime = System.currentTimeMillis();
            for (GenaroidPlugin plugin : pluginsManager.getPlugins()) {
                long pluginStartTime = System.currentTimeMillis();
                plugin.process();
                if (genaroidEnvironment.isDebugMode()) {
                    long processTime = System.currentTimeMillis() - pluginStartTime;
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, plugin.getClass().getSimpleName() + " time: " + processTime + "ms");
                }
            }
            if (genaroidEnvironment.isDebugMode()) {
                long processTime = System.currentTimeMillis() - startTime;
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Genaroid process time: " + processTime + "ms");

                for (GUnit unit : genaroidEnvironment.getUnits()) {
                    try {
                        JavaFileObject source = processingEnv.getFiler().createSourceFile(
                                unit.getCompilationUnit().getPackageName() + "." + unit.getName());
                        Writer writer = source.openWriter();
                        writer.write(getUnitSourceString(unit));
                        writer.flush();
                        writer.close();
                        unit.getCompilationUnit().defs = List.nil();
                    } catch (IOException error) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, Utils.getStackTrace(error));
                    }
                }
            }
        } catch (Throwable e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Utils.getStackTrace(e));
            return false;
        }

        return true;
    }

    private String getUnitSourceString(GUnit unit) {
        return unit.getCompilationUnit().toString()
                // remove empty constructors
                .replaceAll("\\s*(public|private)\\s+\\w*\\(\\)\\s*\\{\\s*super\\(\\);\\s*\\}", "");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        for (GenaroidPlugin processor : pluginsManager.getPlugins()) {
            types.addAll(processor.getSupportedAnnotationTypes());
        }
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> types = new HashSet<>();
        types.add(GenaroidEnvironment.DEBUG_MODE_OPTION_NAME);
        for (GenaroidPlugin processor : pluginsManager.getPlugins()) {
            types.addAll(processor.getSupportedOptions());
        }
        return types;
    }
}
