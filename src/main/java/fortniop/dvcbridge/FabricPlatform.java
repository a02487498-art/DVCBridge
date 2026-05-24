package fortniop.dvcbridge;

import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

import static fortniop.dvcbridge.Constants.PLUGIN_ID;
import static fortniop.dvcbridge.Core.api;

public class FabricPlatform implements Platform {
    private static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_ID);

    @Override
    public boolean isValidPlayer(CommandContext<?> sender) {
        return ((CommandSourceStack) sender.getSource()).getPlayer() != null;
    }

    @Override
    public ServerPlayer commandContextToPlayer(CommandContext<?> context) {
        return api.fromServerPlayer(((CommandSourceStack) context.getSource()).getPlayer());
    }

    @Override
    public @Nullable Position getEntityPosition(ServerLevel level, UUID uuid) {
        net.minecraft.server.level.ServerLevel world = (net.minecraft.server.level.ServerLevel) level.getServerLevel();
        Entity entity = world.getEntity(uuid);
        if (entity == null) {
            return null;
        }
        return api.createPosition(entity.getX(), entity.getY(), entity.getZ());
    }

    @Override
    public boolean isOperator(CommandContext<?> sender) {
        var stack = (CommandSourceStack) sender.getSource();
        var server = stack.getServer();
        PermissionLevel requiredLevel = server.operatorUserPermissions().level();
        return stack.permissions().hasPermission(new Permission.HasCommandLevel(requiredLevel));
    }

    @Override
    public boolean hasPermission(CommandContext<?> sender, String permission) {
        return isOperator(sender);
    }

    @Override
    public void sendMessage(CommandContext<?> sender, Component... message) {
        ((CommandSourceStack) sender.getSource()).sendSystemMessage(toNative(message));
    }

    @Override
    public void sendMessage(Player player, Component... message) {
        ((net.minecraft.server.level.ServerPlayer) player.getPlayer()).sendSystemMessage(toNative(message));
    }

    private net.minecraft.network.chat.Component toNative(Component... message) {
        MutableComponent nativeText = null;

        for (var component : message) {
            MutableComponent mapped = net.minecraft.network.chat.Component.literal(component.text())
                    .withStyle(switch (component.color()) {
                        case WHITE -> ChatFormatting.WHITE;
                        case RED -> ChatFormatting.RED;
                        case YELLOW -> ChatFormatting.YELLOW;
                        case GREEN -> ChatFormatting.GREEN;
                    });
            if (nativeText == null) {
                nativeText = mapped;
            } else {
                nativeText = nativeText.append(mapped);
            }
        }

        if (nativeText == null) {
            return net.minecraft.network.chat.Component.empty();
        }
        return nativeText;
    }

    @Override
    public String getName(Player player) {
        return ((net.minecraft.world.entity.player.Player) player.getPlayer()).getName().getString();
    }

    @Override
    public void setOnPlayerLeaveHandler(Consumer<UUID> handler) {
        ServerPlayConnectionEvents.DISCONNECT.register((networkHandler, server) -> handler.accept(networkHandler.player.getUUID()));
    }

    @Override
    public String getConfigPath() {
        return "config/dvcbridge.yml";
    }

    private static final String LOG_PREFIX = "[" + PLUGIN_ID + "] {}";

    @Override
    public void info(String message) {
        LOGGER.info(LOG_PREFIX, message);
    }

    @Override
    public void warn(String message) {
        LOGGER.warn(LOG_PREFIX, message);
    }

    @Override
    public void error(String message) {
        LOGGER.error(LOG_PREFIX, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        LOGGER.error(LOG_PREFIX, message, throwable);
    }
}
