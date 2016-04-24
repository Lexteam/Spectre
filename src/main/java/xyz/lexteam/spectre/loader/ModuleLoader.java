/*
 * This file is part of Spectre, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, Lexteam <http://www.lexteam.xyz/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package xyz.lexteam.spectre.loader;

import xyz.lexteam.spectre.Module;
import xyz.lexteam.spectre.ModuleContainer;
import xyz.lexteam.spectre.loader.hook.Hook;
import xyz.lexteam.spectre.loader.hook.HookInfo;
import xyz.lexteam.spectre.loader.hook.ReturnableHook;
import xyz.lexteam.spectre.loader.hook.key.HookKey;
import xyz.lexteam.spectre.loader.hook.Hooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The module loader finds modules and loads them.
 * It has 'hooks' of which can be used to change the way the module is instantiated for instance.
 */
public class ModuleLoader {

    private final Map<HookKey, Hook> hookRegistry = new HashMap();
    private File modulesDir;

    /**
     * Constructs a new module loader, where the modules directory is set the the 'modules' directory in the working
     * directory.
     */
    public ModuleLoader() {
        this(new File(".", "modules"));
    }

    /**
     * Constructs a new module loader, with the specified modules directory.
     *
     * @param modulesDir The modules directory
     */
    public ModuleLoader(File modulesDir) {
        this.modulesDir = modulesDir;

        if (!modulesDir.exists()) {
            modulesDir.mkdirs();
        }

        this.registerHook(Hooks.FIND_MAIN_CLASS, new ReturnableHook<String>() {
            @Override
            public String execute(HookInfo info) {
                try {
                    URL url = new URL("jar:file:" + info.get(File.class).getAbsolutePath() + "!/module.info");
                    BufferedReader descriptorReader = new BufferedReader(new InputStreamReader(url.openStream()));

                    String mainClass = descriptorReader.readLine();

                    descriptorReader.close();
                    return mainClass;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        this.registerHook(Hooks.CONSTRUCT_INSTANCE, new ReturnableHook<Object>() {
            @Override
            public Object execute(HookInfo info) {
                try {
                    return info.get(Class.class).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    /**
     * Gets the hook from the specified hook key.
     *
     * @param key The key
     * @return The hook
     */
    private <T extends Hook> T getHook(HookKey<T> key) {
        return (T) this.hookRegistry.get(key);
    }

    /**
     * Registers the hook, with the specified hook key.
     *
     * @param key The key
     * @param hook The hook
     */
    public <T extends Hook>  void registerHook(HookKey<T> key, T hook) {
        this.hookRegistry.put(key, hook);
    }

    /**
     * Finds and loads all the modules in the module directory specified in the constructor.
     *
     * @return A list of module containers
     */
    public List<ModuleContainer> loadAllModules() {
        List<ModuleContainer> modules = new ArrayList<>();

        // Find all jars in the module directory
        File[] jarFiles = this.modulesDir.listFiles(file -> {
            return file.getName().endsWith(".jar");
        });

        for (File jarFile : jarFiles) {
            // Get the module descriptor
            HookInfo descriptorInfo = new HookInfo();
            descriptorInfo.put(File.class, jarFile);
            String mainClass = this.getHook(Hooks.FIND_MAIN_CLASS).execute(descriptorInfo);

            // Get the module class
            try {
                ModuleClassLoader classLoader =
                        new ModuleClassLoader(jarFile.toURI().toURL(), ModuleLoader.class.getClassLoader());
                Class<?> moduleClass = classLoader.loadClass(mainClass);

                // Check module has the @Module annotation
                if (moduleClass.isAnnotationPresent(Module.class)) {
                    // Get annotation
                    Module module = moduleClass.getDeclaredAnnotation(Module.class);

                    // Instantiate the module class
                    HookInfo constructInfo = new HookInfo();
                    constructInfo.put(Class.class, moduleClass);
                    Object instance = this.getHook(Hooks.CONSTRUCT_INSTANCE).execute(constructInfo);

                    // Create container
                    modules.add(new ModuleContainer() {
                        @Override
                        public String getId() {
                            return module.id();
                        }

                        @Override
                        public String getName() {
                            return module.name();
                        }

                        @Override
                        public String getVersion() {
                            return module.version();
                        }

                        @Override
                        public Object getInstance() {
                            return instance;
                        }
                    });
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return modules;
    }
}
