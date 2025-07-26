package net.sixik.researchtree.api.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.functions.BaseFunction;

import java.util.Collection;

public interface FunctionSupport {

    Collection<BaseFunction> getFunctions();

    default void addFunction(BaseFunction function) {
        getFunctions().add(function);
    }

    default void addFunction(Collection<BaseFunction> functions) {
        getFunctions().addAll(functions);
    }

    default Collection<BaseFunction> getFunctions(BaseFunction.ExecuteStage executeStage) {
        return getFunctions().stream().filter(func -> func.getExecuteStage() == executeStage).toList();
    }

    default void executeFunction(BaseFunction.ExecuteStage executeStage, ServerPlayer serverPlayer, BaseResearch research) {
        getFunctions().stream().filter(func -> func.getExecuteStage() == executeStage).forEach(s -> s.execute(serverPlayer, research));
    }
}
