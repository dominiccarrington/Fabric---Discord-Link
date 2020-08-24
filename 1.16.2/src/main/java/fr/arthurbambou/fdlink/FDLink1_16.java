package fr.arthurbambou.fdlink;

import fr.arthurbambou.fdlink.compat_1_16.Message1_16;
import fr.arthurbambou.fdlink.compat_1_16.MessagePacket1_16;
import fr.arthurbambou.fdlink.compat_1_16.MinecraftServer1_16;
import fr.arthurbambou.fdlink.mixin_1_16.TranslatableTextAccessor;
import fr.arthurbambou.fdlink.versionhelpers.ArgAccessor;
import fr.arthurbambou.fdlink.versionhelpers.CrossVersionHandler;
import fr.arthurbambou.fdlink.versionhelpers.MessageSender;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.Message;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.MessagePacket;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.MinecraftServer;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.style.Style;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.UUID;

public class FDLink1_16 implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        FDLink.LOGGER.info("Initializing 1.16 Compat module");
        if (canLoad(CrossVersionHandler.getMinecraftVersion(), "1.16-Snapshot.20.17.a")) {
            ServerTickEvents.START_SERVER_TICK.register((server -> FDLink.getDiscordBot().serverTick(new MinecraftServer1_16(server))));
        }
        if (canLoad(CrossVersionHandler.getMinecraftVersion(), "1.16-Snapshot.20.21.a")) {
            CrossVersionHandler.registerMessageSender(new MessageSender() {
                @Override
                public boolean isCompatibleWithVersion(SemanticVersion semanticVersion) {
                    return canLoad(semanticVersion, "1.16-Snapshot.20.21.a");
                }

                @Override
                public void sendMessageToChat(MinecraftServer server, String message, Style style) {
                    Message literalText = new Message1_16(message);
                    if (style != null) {
                        literalText = literalText.setStyle(style);
                    }
                    server.sendMessageToAll(new MessagePacket1_16(literalText, MessagePacket.MessageType.CHAT, UUID.randomUUID()));
                }
            });
        }
        CrossVersionHandler.registerArgAccessor(new ArgAccessor() {
            @Override
            public boolean isCompatibleWithVersion(SemanticVersion semanticVersion) {
                return true;
            }

            @Override
            public Object[] getArgs(Text translatableText) {
                return ((TranslatableTextAccessor)(TranslatableText)translatableText).getArgs();
            }
        });
        if (canLoad(CrossVersionHandler.getMinecraftVersion(), "1.14")) {
            ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> FDLink.getDiscordBot().serverStarting());
            ServerLifecycleEvents.SERVER_STARTED.register((server -> FDLink.getDiscordBot().serverStarted(new MinecraftServer1_16(server))));
            ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> FDLink.getDiscordBot().serverStopping());
            ServerLifecycleEvents.SERVER_STOPPED.register((server -> FDLink.getDiscordBot().serverStopped()));
        }
    }

    public static boolean canLoad(SemanticVersion semanticVersion, String otherVersion) {
        try {
            int comparison = SemanticVersion.parse(otherVersion).compareTo(semanticVersion);
            return comparison <= 0;
        } catch (VersionParsingException versionParsingException) {
            versionParsingException.printStackTrace();
        }
        return false;
    }
}