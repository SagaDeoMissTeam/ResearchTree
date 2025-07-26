package net.sixik.researchtree.api;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import dev.latvian.mods.rhino.CustomFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.compat.crafttweaker.CraftTweakerScriptContext;
import net.sixik.researchtree.compat.kubejs.KubejSScriptContext;
import net.sixik.researchtree.registers.ModRegisters;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.ResearchShowType;
import net.sixik.researchtree.research.ResearchHideTypeRender;
import net.sixik.researchtree.research.functions.BaseFunction;
import net.sixik.researchtree.research.functions.ScriptFunction;
import net.sixik.researchtree.research.requirements.Requirements;
import net.sixik.researchtree.research.rewards.Reward;
import net.sixik.researchtree.research.triggers.BaseTrigger;
import net.sixik.researchtree.research.triggers.LocateType;
import net.sixik.researchtree.utils.ResearchIconHelper;
import org.openzen.zencode.java.ZenCodeType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


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
        protected CompoundTag iconNbt = new CompoundTag();
        protected List<String> descriptions = new ArrayList<>();
        protected List<RequirementBuilder> requirementBuilders = new ArrayList<>();
        protected List<RewardBuilder> rewardBuilders = new ArrayList<>();
        protected List<TriggerBuilder> triggerBuilders = new ArrayList<>();
        protected List<ResourceLocation> parents = new ArrayList<>();
        protected List<BaseFunction> onStartFunction = new ArrayList<>();
        protected List<BaseFunction> onEndFunction = new ArrayList<>();
        protected ResearchShowType showType = ResearchShowType.SHOW;
        protected ResearchHideTypeRender hideTypeRender = ResearchHideTypeRender.HIDE_STYLE;
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
            requirementBuilders.add(new RequirementBuilder(this, id, arg));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addReward(String id, Object... arg) {
            rewardBuilders.add(new RewardBuilder(this, id, arg));
            return this;
        }


        @ZenCodeType.Method
        public ResearchBuilder addTrigger(String id, Object... arg) {
            triggerBuilders.add(new TriggerBuilder(this, id, arg));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addCustomTrigger(BiFunction<ServerPlayer, BaseResearch, Boolean> function) {
            triggerBuilders.add(new TriggerBuilder(this, "custom_trigger", new Object[]{ function }));
            return this;
        }

        @ZenCodeType.Method
        public TriggerBuilder addTriggerBuilder(String id, Object... arg) {
            TriggerBuilder builder = new TriggerBuilder(this, id, arg);
            triggerBuilders.add(builder);
            return builder;
        }

        @ZenCodeType.Method
        public TriggerBuilder addCustomTriggerBuilder(BiFunction<ServerPlayer, BaseResearch, Boolean> function) {
            TriggerBuilder builder = new TriggerBuilder(this, "custom_trigger", new Object[]{ function });
            triggerBuilders.add(builder);
            return builder;
        }

        @ZenCodeType.Method
        public RequirementBuilder addRequirementBuilder(String id, Object... arg) {
            RequirementBuilder t = new RequirementBuilder(this, id, arg);
            requirementBuilders.add(t);
            return t;
        }

        @ZenCodeType.Method
        public RewardBuilder addRewardBuilder(String id, Object... arg) {
            var t = new RewardBuilder(this, id, arg);
            rewardBuilders.add(t);
            return t;
        }

        @ZenCodeType.Method
        public ResearchBuilder addDescription(String text) {
            descriptions.add(text);
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addIcon(String type, Object object) {
            object = context.convert(object);

            if(Objects.equals(type, "texture")) {
                if(object instanceof String str)
                    this.iconId = ResourceLocation.tryParse(str);
                else if(object instanceof ResourceLocation location)
                    this.iconId = location;
                else
                    context.error("Can't add icon because Input 'object' not 'String' or 'ResourceLocation'");
            } else if(Objects.equals(type, "item")) {
                CompoundTag compoundTag = new CompoundTag();

                if(object instanceof ItemStack itemStack)
                    ResearchIconHelper.putItemIcon(compoundTag, itemStack);
                else if(object instanceof Item item)
                    ResearchIconHelper.putItemIcon(compoundTag, item);
                else
                    context.error("Can't add icon because Input 'object' not 'ItemStack' or 'Item'");

                this.iconNbt = compoundTag;
            } else {
                context.error("Can't add icon because Input id is not are 'texture' or 'item'");
            }

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

        @ZenCodeType.Method
        public ResearchBuilder showType(int type) {
            this.showType = ResearchShowType.values()[Mth.clamp(type, 0, ResearchShowType.values().length)];
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder hideTypeRender(int type) {
            this.hideTypeRender = ResearchHideTypeRender.values()[Math.clamp(type, 0, ResearchHideTypeRender.values().length)];
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addFunctionOnStart(int executeStage, String functionId, Object... args) {
            return addFunction(this, onStartFunction, executeStage, functionId, args);
        }

        @ZenCodeType.Method
        public ResearchBuilder addFunctionOnEnd(int executeStage, String functionId, Object... args) {
            return addFunction(this, onEndFunction, executeStage, functionId, args);
        }

        @ZenCodeType.Method
        public ResearchBuilder addCustomFunctionOnStart(int executeStage, BiConsumer<ServerPlayer, BaseResearch> function) {
            this.onStartFunction.add(new ScriptFunction(function, executeStage));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder addCustomFunctionOnEnd(int executeStage, BiConsumer<ServerPlayer, BaseResearch> function) {
            this.onEndFunction.add(new ScriptFunction(function, executeStage));
            return this;
        }

        protected <T> T addFunction(T returnObj, Collection<BaseFunction> funcList, int executeStage, String functionId, Object... arg) {
            Optional<Supplier<BaseFunction>> funcOpt = ModRegisters.getFunctionSupplier(functionId);
            if(funcOpt.isEmpty()) {
                context.error("Can't find function with id '" + functionId + "'");
                return returnObj;
            }

            BaseFunction func = funcOpt.get().get();
            if(func.internalCheckErrors(context, arg)) {
                func.setStage(executeStage);
                func.setArgs(arg);
                funcList.add(func);
            }
            return returnObj;
        }


        protected BaseResearch complete() {
            BaseResearch baseResearch = new BaseResearch(researchId, iconId);
            baseResearch.setDescriptionRaw(descriptions);

            List<BaseTrigger> triggers = new ArrayList<>();

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

            for (TriggerBuilder triggerBuilder : triggerBuilders) {
                Optional<BaseTrigger> arg = triggerBuilder.complete();
                if(arg.isEmpty()) continue;
                triggers.add(arg.get());
            }

            baseResearch.setTriggers(triggers);
            baseResearch.setFunctions(onStartFunction, onEndFunction);
            baseResearch.setIconNbt(iconNbt);

            baseResearch.showType = showType;
            baseResearch.hideTypeRender = hideTypeRender;
            baseResearch.researchStopping = researchStopping;
            baseResearch.researchTime = researchTime;
            baseResearch.shouldRenderConnection = shouldRenderConnection;
            baseResearch.refundPercent = refundPercent;

            return baseResearch;
        }
    }

    @ZenRegister
    @ZenCodeType.Name("mods.researchtree.RequirementBuilder")
    public class RequirementBuilder {
        protected String id;
        protected Object[] arg;
        protected List<String> tooltip = new ArrayList<>();
        private final ResearchBuilder builder;

        public RequirementBuilder(ResearchBuilder builder, String id, Object... arg) {
            this.id = id;
            this.arg = arg;
            this.builder = builder;
        }

        @ZenCodeType.Method
        public RequirementBuilder addTooltip(String... str) {
            tooltip.addAll(Arrays.asList(str));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder end() {
            return builder;
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
                if(!tooltip.isEmpty())
                    reward.addTooltip(tooltip);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                context.error(e.getMessage());
            }

            return Optional.ofNullable(reward);
        }
    }

    @ZenRegister
    @ZenCodeType.Name("mods.researchtree.RewardBuilder")
    public class RewardBuilder {
        protected String id;
        protected Object[] arg;
        protected List<String> tooltip = new ArrayList<>();
        private final ResearchBuilder builder;

        public RewardBuilder(ResearchBuilder builder, String id, Object... arg) {
            this.id = id;
            this.arg = arg;
            this.builder = builder;
        }

        @ZenCodeType.Method
        public RewardBuilder addTooltip(String... str) {
            tooltip.addAll(Arrays.asList(str));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder end() {
            return builder;
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

                if(!tooltip.isEmpty())
                    reward.addTooltip(tooltip);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                context.error(e.getMessage());
            }

            return Optional.ofNullable(reward);
        }
    }


    @ZenRegister
    @ZenCodeType.Name("mods.researchtree.TriggerBuilder")
    public class TriggerBuilder {
        protected String id;
        protected Object[] arg;
        protected List<BaseFunction> functions;
        private final ResearchBuilder builder;

        public TriggerBuilder(ResearchBuilder builder, String id, Object[] arg) {
            this.id = id;
            this.arg = arg;
            this.functions = new ArrayList<>();
            this.builder = builder;
        }

        @ZenCodeType.Method
        public TriggerBuilder addFunction(String id, Object... args) {
            return builder.addFunction(this, functions, 2, id, args);
        }

        @ZenCodeType.Method
        public TriggerBuilder addCustomFunction(BiConsumer<ServerPlayer, BaseResearch> function) {
            this.functions.add(new ScriptFunction(function, 2));
            return this;
        }

        @ZenCodeType.Method
        public ResearchBuilder end() {
            return builder;
        }

        protected Optional<BaseTrigger> complete() {
            Optional<Function<Void, BaseTrigger>> opt = ModRegisters.getTriggerFunc(id);
            if(opt.isEmpty()) {
                context.error("Can't find Tigger with id [{}]", id);
                return Optional.empty();
            }

            if(Objects.equals(id, "locate_type") && arg[0] instanceof Integer integer)
                arg[0] = LocateType.values()[integer];

            Function<Void, BaseTrigger> constr = opt.get();
            BaseTrigger created = constr.apply(null);

            Class<?>[] classes = new Class[arg.length];
            for (int i = 0; i < arg.length; i++) {
                arg[i] = context.convert(arg[i]);
                classes[i] = arg[i].getClass();
            }
            BaseTrigger reward = null;

            try {
                Constructor<? extends BaseTrigger> d1 = created.getClass().getConstructor(classes);
                reward = d1.newInstance(arg);
                reward.addFunction(functions);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                context.error(e.getMessage());
            }

            return Optional.ofNullable(reward);
        }
    }
}
