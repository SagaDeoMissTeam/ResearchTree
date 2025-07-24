package net.sixik.researchtree.api;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import net.minecraft.resources.ResourceLocation;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.compat.crafttweaker.CraftTweakerScriptContext;
import net.sixik.researchtree.compat.kubejs.KubejSScriptContext;
import net.sixik.researchtree.registers.ModRegisters;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.research.requirements.Requirements;
import net.sixik.researchtree.research.rewards.Reward;
import net.sixik.researchtree.utils.ResearchUtils;
import org.openzen.zencode.java.ZenCodeType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


@ZenRegister
@ZenCodeType.Name("mods.researchtree.ResearchTreeBuilder")
public class ResearchTreeBuilder {

    public static ConcurrentHashMap<ResourceLocation, ResearchData> DATA_BUILDER = new ConcurrentHashMap<>();

    protected final ScriptContext context;
    protected ResourceLocation treeId;
    protected List<ResearchBuilder> researchBuilders;

    public ResearchTreeBuilder(ResourceLocation treeId, ScriptContext context) {
        this.treeId = treeId;
        this.context = context;
        this.researchBuilders = new ArrayList<>();
    }

    public static ResearchTreeBuilder create(ResourceLocation treeId) {
        return new ResearchTreeBuilder(treeId, new KubejSScriptContext());
    }

    public static ResearchTreeBuilder create(ResourceLocation treeId, ScriptContext context) {
        return new ResearchTreeBuilder(treeId, context);
    }

    @ZenCodeType.Method
    public ResearchBuilder addResearch(ResourceLocation researchId) {
        ResearchBuilder builder = new ResearchBuilder(researchId);
        researchBuilders.add(builder);
        return builder;
    }

    @ZenCodeType.Method
    public void build() {
        ResearchData researchData = new ResearchData(treeId);
        for (ResearchBuilder researchBuilder : researchBuilders) {
            researchData.addResearch(researchBuilder.complete());
        }

        DATA_BUILDER.put(researchData.getId(), researchData);
    }

    @ZenCodeType.Method("create")
    public static ResearchTreeBuilder createCraftTweaker(ResourceLocation treeId) {
        return create(treeId, new CraftTweakerScriptContext());
    }

    @ZenRegister
    @ZenCodeType.Name("mods.researchtree.ResearchBuilder")
    public class ResearchBuilder {
        protected ResourceLocation researchId;
        protected ResourceLocation iconId;
        protected List<String> descriptions = new ArrayList<>();
        protected List<RequirementBuilder> requirementBuilders = new ArrayList<>();
        protected List<RewardBuilder> rewardBuilders = new ArrayList<>();
        protected List<ResourceLocation> parents = new ArrayList<>();
        protected long researchTime = -1L;
        protected boolean researchStopping =  true;
        protected boolean shouldRenderConnection = true;
        protected double refundPercent = -1;

        public ResearchBuilder(ResourceLocation researchId) {
            this(researchId, null);
        }

        public ResearchBuilder(ResourceLocation researchId, ResourceLocation iconId) {
            this.researchId = researchId;
            this.iconId = iconId;
        }

        @ZenCodeType.Method
        public ResearchBuilder addParent(ResourceLocation location) {
            parents.add(location);
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addRequirement(String id, Object... arg) {
            requirementBuilders.add(new RequirementBuilder(id, arg));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addReward(String id, Object... arg) {
            rewardBuilders.add(new RewardBuilder(id, arg));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addDescription(String text) {
            descriptions.add(text);
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addIcon(ResourceLocation location) {
            this.iconId = location;
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder stopping(boolean value) {
            this.researchStopping = value;
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder researchTime(long time) {
            this.researchTime = time;
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder shouldRenderConnection(boolean value) {
            this.shouldRenderConnection = value;
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder refundPercent(double percent) {
            this.refundPercent = Math.clamp(percent, 0, 100);
            return this;
        }

        protected BaseResearch complete() {
            BaseResearch baseResearch = new BaseResearch(researchId, iconId);
            baseResearch.setDescriptionRaw(descriptions);

            for (RequirementBuilder requirementBuilder : requirementBuilders) {
                Optional<Requirements> arg = requirementBuilder.complete();
                if(arg.isEmpty()) continue;
                baseResearch.addRequirements(arg.get());
            }

            for (RewardBuilder rewardBuilder : rewardBuilders) {
                Optional<Reward> arg = rewardBuilder.complete();
                if(arg.isEmpty()) continue;
                baseResearch.addReward(arg.get());
            }

            for (ResourceLocation parent : parents) {
                baseResearch.addParent(parent);
            }

            baseResearch.researchStopping = researchStopping;
            baseResearch.researchTime = researchTime;
            baseResearch.shouldRenderConnection = shouldRenderConnection;
            baseResearch.refundPercent = refundPercent;

            return baseResearch;
        }
    }

    public class RequirementBuilder {
        protected String id;
        protected Object[] arg;

        public RequirementBuilder(String id, Object... arg) {
            this.id = id;
            this.arg = arg;
        }

        protected Optional<Requirements> complete() {
            Optional<Function<Void, Requirements>> opt = ModRegisters.getRequirementsFunc(id);
            if(opt.isEmpty()) {
                context.error("Can't find Requirements with id [{}]", id);
                return Optional.empty();
            }

            Function<Void, Requirements> constr = opt.get();
            Requirements created = constr.apply(null);

            Class<?>[] classes = new Class[arg.length];
            for (int i = 0; i < arg.length; i++) {
                arg[i] = context.convert(arg[i]);
                classes[i] = arg[i].getClass();
            }
            Requirements reward = null;

            try {
                Constructor<? extends Requirements> d1 = created.getClass().getConstructor(classes);
                reward = d1.newInstance(arg);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                context.error(e.getMessage());
            }

            return Optional.ofNullable(reward);
        }
    }

    public class RewardBuilder {
        protected String id;
        protected Object[] arg;

        public RewardBuilder(String id, Object... arg) {
            this.id = id;
            this.arg = arg;
        }

        protected Optional<Reward> complete() {
            Optional<Function<Void, Reward>> opt = ModRegisters.getRewardFunc(id);
            if(opt.isEmpty()) {
                context.error("Can't find Reward with id [{}]", id);
                return Optional.empty();
            }

            Function<Void, Reward> constr = opt.get();
            Reward created = constr.apply(null);

            Class<?>[] classes = new Class[arg.length];
            for (int i = 0; i < arg.length; i++) {
                arg[i] = context.convert(arg[i]);
                classes[i] = arg[i].getClass();
            }
            Reward reward = null;

            try {
                Constructor<? extends Reward> d1 = created.getClass().getConstructor(classes);
                reward = d1.newInstance(arg);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                context.error(e.getMessage());
            }

            return Optional.ofNullable(reward);
        }
    }
}
