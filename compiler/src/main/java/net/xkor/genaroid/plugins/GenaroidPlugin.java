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

import android.support.annotation.NonNull;

import net.xkor.genaroid.GenaroidEnvironment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class GenaroidPlugin {

    @NotNull
    private final Set<Class<? extends GenaroidPlugin>> dependencies = new HashSet<>();
    private PluginsManager pluginsManager;
    private GenaroidEnvironment environment;

    public abstract void process();

    @NonNull
    public abstract Set<String> getSupportedAnnotationTypes();

    @NonNull
    public final Set<Class<? extends GenaroidPlugin>> getDependencies() {
        return dependencies;
    }

    public final void addDependency(@NotNull Class<? extends GenaroidPlugin> pluginClass) {
        dependencies.add(pluginClass);
    }

    protected final void init(@NotNull PluginsManager pluginsManager, @NotNull GenaroidEnvironment environment) {
        this.pluginsManager = pluginsManager;
        this.environment = environment;
        Class<?> clazz = this.getClass();
        while (!clazz.equals(GenaroidPlugin.class)) {
            Dependencies pluginDependencies = clazz.getAnnotation(Dependencies.class);
            if (pluginDependencies != null) {
                dependencies.addAll(Arrays.asList(pluginDependencies.value()));
            }

            clazz = clazz.getSuperclass();
        }
        init();
    }

    protected void init() {
    }

    @Nullable
    protected final <T extends GenaroidPlugin> T getPlugin(Class<T> pluginClass) {
        return pluginsManager.getPlugin(pluginClass);
    }

    protected GenaroidEnvironment getEnvironment() {
        return environment;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dependencies {
        Class<? extends GenaroidPlugin>[] value() default {};
    }

}
