package net.sixik.researchtree.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public interface TooltipSupport {

    List<String> getTooltipList();

    default void addTooltip(String str) {
        getTooltipList().add(str);
    }

    default List<Component> getTooltipReady(HolderLookup.Provider provider) {
        List<Component> components = new ArrayList<>();
        for (String s : getTooltipList()) {
            components.add(TextUtils.parseRawText(s, provider));
        }

        return components;
    }
}
