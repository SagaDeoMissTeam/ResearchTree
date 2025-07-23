package net.sixik.researchtree.research.listener;

import net.sixik.researchtree.research.BaseResearch;

public interface ResearchEvent {

    void onResearchEvent(BaseResearch research, ResearchEventType type);
}
