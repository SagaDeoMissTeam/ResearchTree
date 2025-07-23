package net.sixik.researchtree.registers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.sixik.researchtree.research.requirements.Requirements;
import net.sixik.researchtree.research.requirements.ItemRequirements;
import net.sixik.researchtree.research.rewards.ItemReward;
import net.sixik.researchtree.research.rewards.Reward;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ModRegisters {

    public static final String ID_KEY = "object_id";
    public static final String DATA_KEY = "data";


    private static final Map<String, Codec<Requirements>> REQUIREMENTS = new HashMap<>();
    private static final Map<String, Codec<Reward>> REWARDS = new HashMap<>();
    private static final Map<String, Function<Void, Requirements>> REQUIREMENTS_FUNC = new HashMap<>();
    private static final Map<String, Function<Void, Reward>> REWARDS_FUNC = new HashMap<>();


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



    public static void init() {
        registerRequirement(ItemRequirements::new);
        registerReward(ItemReward::new);
    }

}
