package net.sixik.researchtree.research.manager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.sixik.researchtree.research.ResearchData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ClientResearchManager extends ResearchManager{

    static ClientResearchManager INSTANCE = new ClientResearchManager();

    public static ClientResearchManager getInstance() {
        return INSTANCE;
    }

    protected @Nullable ResearchData researchData;

    protected ClientResearchManager() {
        INSTANCE = this;
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
