package net.sixik.researchtree.neoforge;

import net.sixik.researchtree.ResearchTree;
import net.neoforged.fml.common.Mod;

@Mod(ResearchTree.MODID)
public final class ResearchTreeNeoForge {
    public ResearchTreeNeoForge() {
        // Run our common setup.
        ResearchTree.init();
    }
}
