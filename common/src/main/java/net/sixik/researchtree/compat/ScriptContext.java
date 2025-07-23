package net.sixik.researchtree.compat;

public abstract class ScriptContext {

    public abstract void log(String message);

    public abstract void log(String message, Object... args);

    public abstract void error(String message);

    public abstract void error(String message, Object... args);

    public abstract void warn(String message);

    public abstract void warn(String message, Object... args);

    public Object convert(Object o) {
        return o;
    }
}
