package net.sixik.researchtree.config;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Optional;

public class ModConfig {

    protected final String name;
    protected final Path path;

    protected SNBTConfig config;

    private BooleanValue asyncResearchManager;
    private BooleanValue researchWhenPlayerOffline;
    private IntValue countResearchATime;
    private LongValue defaultResearchTimeMs;
    private BooleanValue enableInventoryButton;
    private BooleanValue enableKeyBinding;
    private StringValue researchTreeId;
    private DoubleValue defaultRefundPercent;

    public ModConfig(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public void load() {
        loadDefaulted(getConfig(), path, name);
    }

    protected SNBTConfig getConfig() {
        config = SNBTConfig.create(name);

        SNBTConfig subConfig;
        subConfig = config.addGroup("ResearchManager");
        asyncResearchManager = subConfig.addBoolean("AsyncManager", true);
        asyncResearchManager.comment("Performs all logic related to processing research events in a separate thread");
        researchWhenPlayerOffline = subConfig.addBoolean("ResearchWhenPlayerOffline", false);
        researchWhenPlayerOffline.comment("Allows you to conduct research even when the player is not on the server.");

        subConfig = config.addGroup("ResearchSettings");
        countResearchATime = subConfig.addInt("MaxCountResearch", 1, 1, Integer.MAX_VALUE);
        countResearchATime.comment("The maximum number of studies that can take place at a time");
        defaultResearchTimeMs = subConfig.addLong("DefaultResearchTimeMs", 0, 0, Long.MAX_VALUE);
        defaultResearchTimeMs.comment("The time in milliseconds required for the study if the time was not set");
        defaultRefundPercent = subConfig.addDouble("DefaultRefundPercent", 100, 0, 100);
        defaultRefundPercent.comment("The percentage of resources that will be returned to the player when the study is canceled");

        subConfig = config.addGroup("ServerSettings");
        researchTreeId = subConfig.addString("ResearchTreeId", "");
        researchTreeId.comment("The indicator of the research tree that will be opened by default when you press a key or a button in the inventory.\nIf the value is empty, then nothing will happen.");
        enableKeyBinding = subConfig.addBoolean("KeyBinding", true);
        enableKeyBinding.comment("The hotkey for opening the research tree");

        subConfig = config.addGroup("ClientSettings");
        enableInventoryButton = subConfig.addBoolean("InventoryButton", true);
        enableInventoryButton.comment("Displays a button to open the research tree in the inventory");

        return config;
    }

    public boolean getAsyncResearchManager() {
        return asyncResearchManager.get();
    }

    public int getCountResearchATime() {
        return countResearchATime.get();
    }

    public long getDefaultResearchTimeMs() {
        return defaultResearchTimeMs.get();
    }

    public boolean getResearchWhenPlayerOffline() {
        return researchWhenPlayerOffline.get();
    }

    public boolean getEnableInventoryButton() {
        return enableInventoryButton.get();
    }

    public boolean getEnableKeyBinding() {
        return enableKeyBinding.get();
    }

    public double getDefaultRefundPercent() {
        return defaultRefundPercent.get();
    }

    public Optional<ResourceLocation> getResearchTreeId() {
        String str = researchTreeId.get();
        if(str.isEmpty()) return Optional.empty();
        return Optional.ofNullable(ResourceLocation.tryParse(str));
    }

    static void loadDefaulted(SNBTConfig config, Path configDir, String namespace) {
        loadDefaulted(config, configDir, namespace, config.key + ".snbt");
    }


    static void loadDefaulted(SNBTConfig config, Path configDir, String namespace, String filename) {
        Path configPath = configDir.resolve(filename).toAbsolutePath();
        Path defaultPath = Platform.getConfigFolder().resolve(namespace).resolve(filename);
        config.load(configPath, defaultPath, () -> new String[]{"Default config file that will be copied to " + Platform.getGameFolder().relativize(configPath) + " if it doesn't exist!", "Just copy any values you wish to override in here!"});
    }
}
