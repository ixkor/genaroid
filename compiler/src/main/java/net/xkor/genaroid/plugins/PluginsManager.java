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

import net.xkor.genaroid.GenaroidEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.tools.Diagnostic;

public class PluginsManager {

    private final Set<GenaroidPlugin> plugins = new LinkedHashSet<>();
    private final Map<Class<? extends GenaroidPlugin>, GenaroidPlugin> pluginsMap = new HashMap<>();

    public PluginsManager(GenaroidEnvironment environment) {
        ServiceLoader<GenaroidPlugin> serviceLoader = ServiceLoader.load(GenaroidPlugin.class, GenaroidPlugin.class.getClassLoader());
        for (GenaroidPlugin plugin : serviceLoader) {
            pluginsMap.put(plugin.getClass(), plugin);
        }

        Iterator<GenaroidPlugin> pluginsIterator = pluginsMap.values().iterator();
        while (pluginsIterator.hasNext()) {
            GenaroidPlugin plugin = pluginsIterator.next();
            try {
                plugin.init(this, environment);
            } catch (Throwable error) {
                pluginsIterator.remove();
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Can not initialize genaroid plugin " + plugin.getClass().getName());
            }
        }

        for (GenaroidPlugin plugin : pluginsMap.values()) {
            addPlugin(plugin);
        }
    }

    private void addPlugin(GenaroidPlugin plugin) {
        if (plugin != null) {
            for (Class<? extends GenaroidPlugin> dependencyClass : plugin.getDependencies()) {
                addPlugin(getPlugin(dependencyClass));
            }
            plugins.add(plugin);
        }
    }

    public <T extends GenaroidPlugin> T getPlugin(Class<T> pluginClass) {
        return pluginClass.cast(pluginsMap.get(pluginClass));
    }

    public Iterable<GenaroidPlugin> getPlugins() {
        return Collections.unmodifiableCollection(plugins);
    }

}
