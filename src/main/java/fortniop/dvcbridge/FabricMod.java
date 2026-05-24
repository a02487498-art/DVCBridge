package fortniop.dvcbridge;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static fortniop.dvcbridge.Core.*;

public class FabricMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        if (platform == null) {
            platform = new FabricPlatform();
        }

        enable();

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(SubCommands.build(literal("dvc")))
        );

        ServerLifecycleEvents.SERVER_STOPPED.register((server -> disable()));
    }
}
