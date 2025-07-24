package net.sixik.researchtree.client.debug;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.ui.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.client.ResearchWidget;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.DebugResearchData;
import net.sixik.researchtree.research.requirements.ItemRequirements;
import net.sixik.researchtree.research.rewards.ItemReward;

import java.util.List;

public class ClientDebugUtils {

    public static void log(String message) {
        if(!Platform.isDevelopmentEnvironment()) return;
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
    }

    public static void log(String message, Object... args) {
        if(!Platform.isDevelopmentEnvironment()) return;
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.format(message, args)));
    }

    public static void createDebugWidgets(Panel panel) {
        if(!Platform.isDevelopmentEnvironment()) {
            return;
        }

        for (BaseResearch research : DebugResearchData.DEFAULT.getResearchList()) {
            ResearchWidget widget = new ResearchWidget(panel, research);
            widget.setPosAndSize(0,0, 100, 20);
            panel.add(widget);
        }
    }

    public static void createDebugWidgets(Panel panel, int multiply) {
        if(!Platform.isDevelopmentEnvironment()) return;

        RandomSource source = RandomSource.create();

        for (BaseResearch research : DebugResearchData.DEFAULT.getResearchList()) {
            int x = source.nextInt(0, Minecraft.getInstance().getWindow().getGuiScaledWidth() * multiply);
            int y = source.nextInt(0, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 * multiply);

            ResearchWidget widget;
            panel.add(widget = new ResearchWidget(panel, research));
            widget.setPosAndSize(x,y, 100, 20);
        }

    }

    public static BaseResearch generateResearch() {
        return new BaseResearch(ResourceLocation.tryBuild(ResearchTree.MODID, String.valueOf(RandomSource.create().nextInt(Integer.MAX_VALUE))));
    }

    public static BaseResearch generateRequirements(BaseResearch research, RandomSource source) {
        return generateRequirements(research, source, 5);
    }

    public static BaseResearch generateRequirements(BaseResearch research, RandomSource source, int count) {
        List<Item> list = BuiltInRegistries.ITEM.stream().toList();

        Item item;

        do {
            item = list.get(source.nextInt(list.size()));
        } while (item == Items.AIR);

        for (int i = 0; i < count; i++) {
            research.addRequirements(new ItemRequirements(item.getDefaultInstance().copyWithCount(source.nextInt(100))));
        }

        return research;
    }

    public static BaseResearch generateRewards(BaseResearch research, RandomSource source) {
        List<Item> list = BuiltInRegistries.ITEM.stream().toList();

        Item item;
        do {
            item = list.get(source.nextInt(list.size()));
        } while (item == Items.AIR);

        int count = source.nextInt(5);
        for (int i = 0; i < count; i++) {
            research.addReward(new ItemReward(item.getDefaultInstance().copyWithCount(source.nextInt(100))));
        }

        return research;
    }
}
