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

import xyz.lexteam.spectre.loader.hook.Hook;
import xyz.lexteam.spectre.loader.hook.HookInfo;
import xyz.lexteam.spectre.loader.hook.HookKey;
import xyz.lexteam.spectre.loader.hook.Hooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The module loader finds modules and loads them.
 * It has 'hooks' of which can be used to change the way the module is instantiated for instance.
 */
public class ModuleLoader {

    private Map<HookKey, Hook> hookRegistry = new HashMap();

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
        this.registerHook(Hooks.READ_DESCRIPTOR, new Hook() {
            @Override
            public void execute(HookInfo info) {
                try {
                    BufferedReader descriptorReader =
                            new BufferedReader(new InputStreamReader(info.get(URL.class).openStream()));
                    info.put("main-class", descriptorReader.readLine());
                    descriptorReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        this.registerHook(Hooks.CONSTRUCT_INSTANCE, new Hook() {
            @Override
            public void execute(HookInfo info) {
                try {
                    info.put("instance", info.get(Class.class).newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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
    private Hook getHook(HookKey key) {
        return this.hookRegistry.get(key);
    }

    /**
     * Registers the hook, with the specified hook key.
     *
     * @param key The key
     * @param hook The hook
     */
    public void registerHook(HookKey key, Hook hook) {
        this.hookRegistry.put(key, hook);
    }
}
