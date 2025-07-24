package net.sixik.researchtree.compat.kubejs;

import dev.latvian.mods.kubejs.script.ConsoleJS;
import net.sixik.researchtree.compat.ScriptContext;

public class KubejSScriptContext extends ScriptContext {
    @Override
    public void log(String message) {
        ConsoleJS.SERVER.log(message);
    }

    @Override
    public void log(String message, Object... args) {
        ConsoleJS.SERVER.log(message, args);
    }

    @Override
    public void error(String message) {
        ConsoleJS.SERVER.error(message);
    }

    @Override
    public void error(String message, Object... args) {
        ConsoleJS.SERVER.log(message, args);
    }

    @Override
    public void warn(String message) {
        ConsoleJS.SERVER.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        ConsoleJS.SERVER.log(message, args);
    }
}
