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
package xyz.lexteam.spectre.loader.hook;

import xyz.lexteam.spectre.loader.hook.key.HookKey;
import xyz.lexteam.spectre.loader.hook.key.ReturnableHookKey;

import java.util.List;

/**
 * A list of all the hooks.
 */
public final class Hooks {

    public static final ReturnableHookKey<List<String>> FIND_MAIN_CLASSES
            = createReturnableHookKey(ReturnableHook.class);
    public static final ReturnableHookKey<Object> CONSTRUCT_INSTANCE
            = createReturnableHookKey(ReturnableHook.class);

    private static final HookKey createVoidableHookKey(Class<? extends Hook> hookClass) {
        return () -> hookClass;
    }

    private static final ReturnableHookKey createReturnableHookKey(Class<ReturnableHook> hookClass) {
        return () -> hookClass;
    }
}
