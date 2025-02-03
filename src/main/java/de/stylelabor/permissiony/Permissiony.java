package de.stylelabor.permissiony;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

@Mod(Permissiony.MODID)
public class Permissiony {
    public static final String MODID = "permissiony";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Permissiony() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static boolean hasPermission(ServerPlayer player, String node) {
        Optional<Boolean> value = FTBRanksAPI.getPermissionValue(player, node).asBoolean();
        return value.isPresent() && value.get();
    }

    public static boolean hasPermissionValue(ServerPlayer player, String node, String value) {
        Optional<String> permValue = FTBRanksAPI.getPermissionValue(player, node).asString();
        return permValue.isPresent() && permValue.get().equals(value);
    }

    public static int getNumericPermission(ServerPlayer player, String node) {
        Optional<Number> value = FTBRanksAPI.getPermissionValue(player, node).asNumber();
        return value.map(Number::intValue).orElse(0);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Clear chat by sending empty messages
            for (int i = 0; i < 100; i++) {
                player.sendSystemMessage(Component.literal(""));
            }

            // Show chunk loading message if player has permission
            if (hasPermission(player, "stylelabor.chunk_load_offline")) {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    server.getCommands().performPrefixedCommand(
                            new CommandSourceStack(
                                    CommandSource.NULL,
                                    player.position(),
                                    player.getRotationVector(),
                                    Objects.requireNonNull(server.getLevel(player.level().dimension())),
                                    4,
                                    player.getName().getString(),
                                    player.getDisplayName(),
                                    server,
                                    player
                            ).withSuppressedOutput(),
                            "openpac player-config for " + player.getName().getString() + " set claims.forceload.offlineForceload true"
                    );

                    Component message = Component.literal("You have chunk loading permissions while offline")
                            .withStyle(ChatFormatting.GREEN);
                    player.sendSystemMessage(message);
                    player.sendSystemMessage(Component.literal("")); // Spacing
                }
            }

            // Add website links for all players
            Component websiteLink = Component.literal("⭐ Website:   stylelabor.de")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://stylelabor.de"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open website"))));

            Component shopLink = Component.literal("🦜 Shop:      shop.stylelabor.de")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://shop.stylelabor.de"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open shop"))));

            player.sendSystemMessage(websiteLink);
            player.sendSystemMessage(shopLink);
        }
    }






}
