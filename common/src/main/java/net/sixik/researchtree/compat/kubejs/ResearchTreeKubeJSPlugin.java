package net.sixik.researchtree.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import net.sixik.researchtree.api.ResearchTreeBuilder;

public class ResearchTreeKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry event) {
        if(event.type().isServer()) {
            event.add("ResearchTreeBuilder", ResearchTreeBuilder.class);
            event.add("ResearchBuilder", ResearchTreeBuilder.ResearchBuilder.class);
            event.add("RequirementBuilder", ResearchTreeBuilder.RequirementBuilder.class);
            event.add("RewardBuilder", ResearchTreeBuilder.RewardBuilder.class);
        }
    }

}
