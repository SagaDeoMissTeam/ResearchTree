package net.sixik.researchtree.research.manager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.sixik.researchtree.api.ResearchTreeBuilder;
import net.sixik.researchtree.research.ResearchData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class ClientResearchManager extends ResearchManager{

    static ClientResearchManager INSTANCE = new ClientResearchManager();

    public static ClientResearchManager getInstance() {
        return INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientResearchManager.class);


    protected @Nullable ResearchData researchData;

    private volatile boolean shutdown = false;
    private final ExecutorService executor;

    protected ClientResearchManager() {
        INSTANCE = this;
        this.executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "ClientResearchManager"));

        executor.submit(() -> {
            while (!shutdown) {
                try {
                    tickResearchData();
                } catch (Exception e) {
                    LOGGER.error("Error processing research task", e);
                }
            }
        });
    }

    @Override
    public void shutdown() {
        if (shutdown) return;
        shutdown = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                LOGGER.warn("ResearchManager executor did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted during ResearchManager shutdown", e);
        }
    }


    public PlayerResearchData getPlayerData() {
        return getOrCreatePlayerData(Minecraft.getInstance().player);
    }

    public Optional<ResearchData> getResearchData() {
        return Optional.ofNullable(researchData);
    }

    public ResearchData getResearchDataOrDefault(ResearchData data) {
        if(researchData == null)
            researchData = data;
        return researchData;
    }

    public void setResearchData(@Nullable ResearchData researchData) {
        this.researchData = researchData;
    }
}
