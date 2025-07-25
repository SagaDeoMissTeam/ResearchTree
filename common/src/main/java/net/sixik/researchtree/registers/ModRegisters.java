package net.sixik.researchtree.registers;

import com.mojang.serialization.Codec;
import dev.architectury.platform.Platform;
import net.sixik.researchtree.compat.ftbteams.FTBTeamManager;
import net.sixik.researchtree.research.functions.BaseFunction;
import net.sixik.researchtree.research.functions.CommandFunction;
import net.sixik.researchtree.research.functions.ScriptFunction;
import net.sixik.researchtree.research.requirements.ItemRequirements;
import net.sixik.researchtree.research.requirements.Requirements;
import net.sixik.researchtree.research.rewards.ItemReward;
import net.sixik.researchtree.research.rewards.Reward;
import net.sixik.researchtree.research.teams.TeamManager;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModRegisters {

    public static final String ID_KEY = "object_id";
    public static final String DATA_KEY = "data";


    private static final Map<String, Codec<Requirements>> REQUIREMENTS = new HashMap<>();
    private static final Map<String, Codec<Reward>> REWARDS = new HashMap<>();
    private static final Map<String, Function<Void, Requirements>> REQUIREMENTS_FUNC = new HashMap<>();
    private static final Map<String, Function<Void, Reward>> REWARDS_FUNC = new HashMap<>();
    private static final List<Supplier<TeamManager>> TEAM_MANAGERS = new ArrayList<>();
    private static final Map<String, Supplier<BaseFunction>> FUNCTION_MAP = new HashMap<>();

    public static void registerFunction(Supplier<BaseFunction> functionSupplier) {
        BaseFunction function = functionSupplier.get();
        FUNCTION_MAP.put(function.getId(), functionSupplier);
    }

    public static void registerTeamManager(Supplier<TeamManager> teamManagerSupplier) {
        TEAM_MANAGERS.add(teamManagerSupplier);
    }

    public static void registerRequirement(Function<Void, Requirements> requirementsFunction) {
        Requirements requirements = requirementsFunction.apply(null);
        REQUIREMENTS.put(requirements.getId(), requirements.codec());
        REQUIREMENTS_FUNC.put(requirements.getId(), requirementsFunction);
    }

    public static void registerReward(Function<Void, Reward> rewardsFunction) {
        Reward reward = rewardsFunction.apply(null);
        REWARDS.put(reward.getId(), reward.codec());
        REWARDS_FUNC.put(reward.getId(), rewardsFunction);
    }

    public static Optional<Codec<Requirements>> getRequirements(String className) {
        var t = REQUIREMENTS.get(className);
        return t == null ? Optional.empty() : Optional.of(t);
    }

    public static Optional<Codec<Reward>> getReward(String className) {
        var t = REWARDS.get(className);
        return t == null ? Optional.empty() : Optional.of(t);
    }

    public static Optional<Function<Void, Requirements>> getRequirementsFunc(String className) {
        Function<Void, Requirements> t = REQUIREMENTS_FUNC.get(className);
        return t == null ? Optional.empty() : Optional.of(t);
    }

    public static Optional<Function<Void, Reward>> getRewardFunc(String className) {
        Function<Void, Reward> t = REWARDS_FUNC.get(className);
        return t == null ? Optional.empty() : Optional.of(t);
    }

    public static Optional<Supplier<BaseFunction>> getFunctionSupplier(String functionId) {
        return Optional.ofNullable(FUNCTION_MAP.get(functionId));
    }

    public static List<Supplier<TeamManager>> getTeamManagers() {
        return new ArrayList<>(TEAM_MANAGERS);
    }

    public static void init() {
        registerRequirement(ItemRequirements::new);
        registerReward(ItemReward::new);

        if(Platform.isModLoaded("ftbteams")) {
            registerTeamManager(FTBTeamManager::new);
        }

        registerFunction(CommandFunction::new);
        registerFunction(ScriptFunction::new);
    }

}
