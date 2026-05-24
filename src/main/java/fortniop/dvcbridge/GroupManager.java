package fortniop.dvcbridge;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.CreateGroupEvent;
import de.maxhenkel.voicechat.api.events.JoinGroupEvent;
import de.maxhenkel.voicechat.api.events.LeaveGroupEvent;
import de.maxhenkel.voicechat.api.events.RemoveGroupEvent;
import fortniop.dvcbridge.util.BiMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import static fortniop.dvcbridge.Core.api;
import static fortniop.dvcbridge.Core.platform;

public final class GroupManager {
    public static final BiMap<UUID, Integer> groupFriendlyIds = new BiMap<>();
    public static final Map<UUID, List<ServerPlayer>> groupPlayers = new HashMap<>();

    public static @Nullable String getPassword(Group group) {
        try {
            Field groupField = group.getClass().getDeclaredField("group");
            groupField.setAccessible(true);
            Object groupObject = groupField.get(group);
            Field passwordField = groupObject.getClass().getDeclaredField("password");
            passwordField.setAccessible(true);
            return (String) passwordField.get(groupObject);
        } catch (Throwable e) {
            platform.error("Could not get password of group \"" + group.getName() + "\" (" + group.getId() + "): ", e);
            return null;
        }
    }

    private static List<ServerPlayer> getPlayers(Group group) {
        List<ServerPlayer> players = groupPlayers.putIfAbsent(group.getId(), new ArrayList<>());
        if (players == null) players = groupPlayers.get(group.getId());
        return players;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void onJoinGroup(JoinGroupEvent event) {
        Group group = event.getGroup();
        ServerPlayer player = event.getConnection().getPlayer();

        List<ServerPlayer> players = getPlayers(group);
        if (players.stream().noneMatch(serverPlayer -> serverPlayer.getUuid() == player.getUuid())) {
            platform.debug(player.getUuid() + " (" + platform.getName(player) + ") joined " + group.getId() + " (" + group.getName() + ")");
            players.add(player);
        } else {
            platform.debug(player.getUuid() + " (" + platform.getName(player) + ") already joined " + group.getId() + " (" + group.getName() + ")");
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void onLeaveGroup(LeaveGroupEvent event) {
        Group group = event.getGroup();
        ServerPlayer player = event.getConnection().getPlayer();
        if (group == null) {
            for (var groupEntry : groupPlayers.entrySet()) {
                List<ServerPlayer> playerList = groupEntry.getValue();
                if (playerList.stream().anyMatch(serverPlayer -> serverPlayer.getUuid() == player.getUuid())) {
                    UUID playerGroup = groupEntry.getKey();
                    platform.debug(player.getUuid() + " (" + platform.getName(player) + ") left " + playerGroup + " (" + api.getGroup(playerGroup).getName() + ")");
                    playerList.remove(player);
                    return;
                }
            }
            platform.debug(player.getUuid() + " (" + platform.getName(player) + ") left a group but we couldn't find the group they left");
            return;
        }

        platform.debug(player.getUuid() + " (" + platform.getName(player) + ") left " + group.getId() + " (" + group.getName() + ")");

        List<ServerPlayer> players = getPlayers(group);
        players.remove(player);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void onGroupCreated(CreateGroupEvent event) {
        Group group = event.getGroup();
        UUID groupId = group.getId();

        if (groupFriendlyIds.get(groupId) == null) {
            int friendlyId = 1;
            Collection<Integer> friendlyIds = groupFriendlyIds.values();
            while (friendlyIds.contains(friendlyId)) {
                friendlyId++;
            }
            groupFriendlyIds.put(groupId, friendlyId);
        }

        VoicechatConnection connection = event.getConnection();
        if (connection == null) {
            platform.debug("someone created " + groupId + " (" + group.getName() + ")");
            return;
        }
        ServerPlayer player = connection.getPlayer();

        platform.debug(player.getUuid() + " (" + platform.getName(player) + ") created " + groupId + " (" + group.getName() + ")");

        List<ServerPlayer> players = getPlayers(group);
        players.add(player);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void onGroupRemoved(RemoveGroupEvent event) {
        Group group = event.getGroup();
        UUID groupId = group.getId();

        platform.debug(groupId + " (" + groupFriendlyIds.get(groupId) + ", " + group.getName() + ")" + " was removed");

        groupPlayers.remove(groupId);
        groupFriendlyIds.remove(groupId);
    }
}
