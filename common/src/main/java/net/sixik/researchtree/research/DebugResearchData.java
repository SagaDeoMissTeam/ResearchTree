package net.sixik.researchtree.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.client.debug.ClientDebugUtils;

import java.util.List;
import java.util.UUID;

public class DebugResearchData extends ResearchData{

    public static DebugResearchData DEFAULT = new DebugResearchData(ResearchData.DEFAULT);

    public DebugResearchData(final ResourceLocation id) {
        super(id);
        RandomSource source = RandomSource.create();
        BaseResearch research = new BaseResearch(ResourceLocation.tryBuild(ResearchTree.MODID, "some_research"));
//        research.addRequirements(new ItemRequirements(Items.DIAMOND.getDefaultInstance().copyWithCount(35)));
//        research.addRequirements(new ItemRequirements(Items.ICE.getDefaultInstance().copyWithCount(142)));
//        research.addRequirements(new ItemRequirements(Items.BEACON.getDefaultInstance().copyWithCount(4)));
//        research.addRequirements(new ItemRequirements(Items.BEDROCK.getDefaultInstance().copyWithCount(536)));
//        research.addRequirements(new ItemRequirements(Items.IRON_INGOT.getDefaultInstance().copyWithCount(128)));
//        research.addRequirements(new ItemRequirements(Items.GOLD_BLOCK.getDefaultInstance().copyWithCount(35)));
//        research.addRequirements(new ItemRequirements(Items.GOLD_INGOT.getDefaultInstance().copyWithCount(105)));
//        research.addRequirements(new ItemRequirements(Items.GOLD_NUGGET.getDefaultInstance().copyWithCount(6)));
//        research.addRequirements(new ItemRequirements(Items.IRON_NUGGET.getDefaultInstance().copyWithCount(12)));
//        research.addRequirements(new ItemRequirements(Items.IRON_BLOCK.getDefaultInstance().copyWithCount(64)));
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRequirements(research, source.fork(), 40);
        ClientDebugUtils.generateRewards(research, source);
        ClientDebugUtils.generateRewards(research, source);
        ClientDebugUtils.generateRewards(research, source);
        ClientDebugUtils.generateRewards(research, source);
        ClientDebugUtils.generateRewards(research, source);
        ClientDebugUtils.generateRewards(research, source);

        research.setDescriptionRaw(List.of("Hello world §6Fobos, §7§oFSA \nBogdan delal eto §rgovno s §1renderom tak sto vozmozni problemi kak vsegda. §rЧё в первыый раз да ?",
                "Hello world §6Fobos, §7§oFSA \nBogdan delal eto §rgovno s §1renderom tak sto vozmozni problemi kak vsegda. §rЧё в первыый раз да ?"));



        add(research);

        BaseResearch research1 = ClientDebugUtils.generateResearch();
        research1.shouldRenderConnection = false;

        for (int i1 = 0; i1 < source.nextInt(1, 5); i1++) {
            research = ClientDebugUtils.generateResearch();
            ClientDebugUtils.generateRequirements(research, source);
            add(research); // Добавляем основное исследование
            for (int i = 0; i < source.nextInt(0, 20); i++) {
                var d1 = ClientDebugUtils.generateResearch();
                ClientDebugUtils.generateRequirements(d1, source);
                add(d1); // Добавляем родителя d1
                if (source.nextBoolean()) {
                    BaseResearch s = ClientDebugUtils.generateResearch();
                    s.shouldRenderConnection = source.nextBoolean();
                    ClientDebugUtils.generateRequirements(s, source);
                    add(s); // Добавляем родителя s
                    d1.addParent(s);
                }
                research.addParent(d1);
                d1.addParent(research1);
            }
        }
    }

    protected BaseResearch add(BaseResearch research) {
        addResearch(research);
        return research;
    }
}
