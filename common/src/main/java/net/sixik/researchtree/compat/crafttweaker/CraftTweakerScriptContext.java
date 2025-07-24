package net.sixik.researchtree.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.compat.ScriptContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class CraftTweakerScriptContext extends ScriptContext {

    public static final Logger LOGGER = CraftTweakerAPI.getLogger(ResearchTree.MODID);

    @Override
    public void log(String message) {
        LOGGER.log(Level.INFO, message);
    }

    @Override
    public void log(String message, Object... args) {
        LOGGER.log(Level.INFO, message, args);
    }

    @Override
    public void error(String message) {
        LOGGER.log(Level.ERROR, message);
    }

    @Override
    public void error(String message, Object... args) {
        LOGGER.log(Level.ERROR, message, args);
    }

    @Override
    public void warn(String message) {
        LOGGER.log(Level.WARN, message);
    }

    @Override
    public void warn(String message, Object... args) {
        LOGGER.log(Level.WARN, message, args);
    }

    @Override
    public Object convert(Object o) {
        if(o instanceof MCItemStack mcItemStack)
            return mcItemStack.getInternal();

        return o;
    }
}
