package net.sixik.researchtree;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.sixik.researchtree.config.ModConfig;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.network.ask.GetAndOpenResearchScreenASK;
import net.sixik.researchtree.registers.ModRegisters;
import org.lwjgl.glfw.GLFW;

public final class ResearchTree {
    public static final String MODID = "researchtree";

    public static final String SDMSHOP_CATEGORY = "key.category.researchtree";
    public static final String KEY_NAME = "key.researchtree.open";
    @Environment(EnvType.CLIENT)
    public static KeyMapping KEY_SHOP = new KeyMapping(KEY_NAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, SDMSHOP_CATEGORY);

    public static final ModConfig MOD_CONFIG =
            new ModConfig("researchtree", Platform.getConfigFolder().resolve("ResearchTree"));

    public static void init() {
        if(Platform.isDevelopmentEnvironment()) {
            Theme.renderDebugBoxes = false;
        }

        if(Platform.getEnv() == EnvType.CLIENT)
            registerKeyBinding();

        ResearchTreeModEvents.init();
        ModRegisters.init();
        ResearchTreeNetwork.init();
        MOD_CONFIG.load();
    }

    @Environment(EnvType.CLIENT)
    public static void registerKeyBinding() {
        KeyMappingRegistry.register(KEY_SHOP);

        ClientTickEvent.CLIENT_PRE.register((instance -> {
            if (KEY_SHOP.consumeClick()) {
                ResearchTree.MOD_CONFIG.getResearchTreeId().ifPresent(s -> {
                    new GetAndOpenResearchScreenASK(null).execute(s);
                });
            }
        }));
    }
}
