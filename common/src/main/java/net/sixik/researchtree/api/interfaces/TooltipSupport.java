package net.sixik.researchtree.api.interfaces;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface TooltipSupport {

    static final String TOOLTIP_KEY = "tooltip_list_key";

    List<String> getTooltipList();

    default void addTooltip(String str) {
        getTooltipList().add(str);
    }

    default void addTooltip(Collection<String> str) {
        getTooltipList().addAll(str);
    }

    List<Component> getCachedTooltip();

    default List<Component> getTooltipReady(HolderLookup.Provider provider) {
        List<Component> components = new ArrayList<>();
        for (String s : getTooltipList()) {
            components.add(TextUtils.parseRawTextFromLocalization(s, provider));
        }

        return components;
    }
}
