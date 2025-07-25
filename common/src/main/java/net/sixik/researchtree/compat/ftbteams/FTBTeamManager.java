package net.sixik.researchtree.compat.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.research.teams.TeamManager;

import java.util.*;

public class FTBTeamManager extends TeamManager {

    @Override
    public Collection<?> getTeams() {
        return FTBTeamsAPI.api().getManager().getTeams();
    }

    @Override
    public Optional<?> getTeamForPlayer(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
    }

    @Override
    public Optional<?> getTeamById(UUID teamId) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamId);
    }

    @Override
    public Collection<UUID> getTeamMembers(ServerPlayer player) {
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        if(team.isEmpty()) return  new ArrayList<>();
        return team.get().getMembers();
    }

    @Override
    public Collection<UUID> getTeamMembers(UUID teamId) {
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamByID(teamId);
        if(team.isEmpty()) return  new ArrayList<>();
        return team.get().getMembers();
    }

    @Override
    public Collection<ServerPlayer> getTeamOnlineMembers(ServerPlayer player) {
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        if(team.isEmpty()) return  new ArrayList<>();
        return team.get().getOnlineMembers();
    }

    @Override
    public Collection<ServerPlayer> getTeamOnlineMembers(UUID teamId) {
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamByID(teamId);
        if(team.isEmpty()) return new ArrayList<>();
        return team.get().getOnlineMembers();
    }

    @Override
    public boolean haveTeam(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).filter(value -> !(value instanceof PlayerTeam)).isPresent();
    }

    @Override
    public boolean teamHaveCustomData(ServerPlayer player) {
        return true;
    }

    @Override
    public boolean teamHaveCustomData(UUID teamId) {
        return true;
    }

    @Override
    public Optional<CompoundTag> getTeamCustomData(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).map(Team::getExtraData);
    }

    @Override
    public Optional<CompoundTag> getTeamCustomData(UUID teamId) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamId).map(Team::getExtraData);
    }
}
