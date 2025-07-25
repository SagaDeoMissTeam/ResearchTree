package net.sixik.researchtree.research.teams;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public abstract class TeamManager {

    public abstract Collection<?> getTeams();
    public abstract Optional<?> getTeamForPlayer(ServerPlayer player);
    public abstract Optional<?> getTeamById(UUID teamId);

    public abstract Collection<UUID> getTeamMembers(ServerPlayer player);
    public abstract Collection<UUID> getTeamMembers(UUID teamId);
    public abstract Collection<ServerPlayer> getTeamOnlineMembers(ServerPlayer player);
    public abstract Collection<ServerPlayer> getTeamOnlineMembers(UUID teamId);

    public Collection<UUID> getOfflineMembers(ServerPlayer player) {
        Collection<UUID> online = getTeamOnlineMembers(player).stream().map(s -> s.getGameProfile().getId()).toList();
        Collection<UUID> member = getTeamMembers(player);
        return member.stream().filter(s -> !online.contains(s)).toList();
    }

    public Collection<UUID> getOfflineMembers(UUID teamId) {
        Collection<UUID> online = getTeamOnlineMembers(teamId).stream().map(s -> s.getGameProfile().getId()).toList();
        Collection<UUID> member = getTeamMembers(teamId);
        return member.stream().filter(s -> !online.contains(s)).toList();
    }

    public abstract boolean haveTeam(ServerPlayer player);

    public abstract boolean teamHaveCustomData(ServerPlayer player);
    public abstract boolean teamHaveCustomData(UUID teamId);

    public abstract Optional<CompoundTag> getTeamCustomData(ServerPlayer player);
    public abstract Optional<CompoundTag> getTeamCustomData(UUID teamId);
}
