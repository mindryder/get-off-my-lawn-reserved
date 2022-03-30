package draylar.goml.other;

import com.jamieswhiteshirt.rtree3i.RTreeMap;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.DataKey;
import draylar.goml.config.GOMLConfig;
import draylar.goml.registry.GOMLEntities;
import draylar.goml.ui.ClaimListGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.literal;

public class ClaimCommand {

    private ClaimCommand() {
        // NO-OP
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(literal("goml")
                    .then(literal("help")
                            .requires(Permissions.require("text.goml.command.command.help", true))
                            .executes(ClaimCommand::help)
                    )
                    .then(literal("trust")
                            .requires(Permissions.require("text.goml.command.command.trust", true))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(context -> trust(context, false))
                            )
                    )
                    .then(literal("untrust")
                            .requires(Permissions.require("text.goml.command.command.untrust", true))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes((ctx) -> ClaimCommand.untrust(ctx, false)))
                    )
                    .then(literal("addowner")
                            .requires(Permissions.require("text.goml.command.command.addowner", true))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(context -> trust(context, true)))
                    )

                    .then(literal("list")
                            .requires(Permissions.require("text.goml.command.command.list", true))
                            .executes(context -> openList(context, context.getSource().getPlayer().getGameProfile()))
                    )

                    .then(literal("gui")
                            .requires(Permissions.require("text.goml.command.command.gui", true))
                            .executes(context -> openGui(context))
                    )


                    .then(literal("admin")
                            .requires(Permissions.require("text.goml.command.command.admin", 3))
                            .then(literal("adminmode")
                                    .requires(Permissions.require("text.goml.command.command.admin.admin_mode", 3))
                                    .executes(ClaimCommand::adminMode)
                            )
                            .then(literal("removeowner")
                                    .requires(Permissions.require("text.goml.command.command.admin.removeowner", true))
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes((ctx) -> ClaimCommand.untrust(ctx, true)))
                            )
                            .then(literal("info")
                                    .requires(Permissions.require("text.goml.command.command.admin.info", 3))
                                    .executes(ClaimCommand::infoAdmin)
                            )
                            .then(literal("world")
                                    .requires(Permissions.require("text.goml.command.command.admin.world", 3))
                                    .executes(ClaimCommand::world)
                            )
                            .then(literal("general")
                                    .requires(Permissions.require("text.goml.command.command.admin.general", 3))
                                    .executes(ClaimCommand::general)
                            )
                            .then(literal("remove")
                                    .requires(Permissions.require("text.goml.command.command.admin.remove", 3))
                                    .executes(ClaimCommand::remove)
                            )
                            .then(literal("reload")
                                    .requires(Permissions.require("text.goml.command.command.admin.reload", 4))
                                    .executes(ClaimCommand::reload)
                            )
                            .then(literal("list")
                                    .requires(Permissions.require("text.goml.command.command.list", true))
                                    .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                var player = GameProfileArgumentType.getProfileArgument(context, "player").toArray(new GameProfile[0]);

                                                if (player.length == 0) {
                                                    context.getSource().sendFeedback(new TranslatableText("argument.player.unknown").formatted(Formatting.RED), false);
                                                }

                                                return openList(context, player[0]);
                                            })
                                    )
                            )
                    )
            );
        });
    }

    private static int adminMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        var newMode = !((GomlPlayer) player).goml_getAdminMode();
        ((GomlPlayer) player).goml_setAdminMode(newMode);
        context.getSource().sendFeedback(prefix(new TranslatableText(newMode ? "text.goml.admin_mode.enabled" : "text.goml.admin_mode.disabled")), false);

        return 1;
    }

    /**
     * Sends the player general information about all claims on the server.
     *
     * @param context context
     * @return success flag
     */
    private static int general(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity player = context.getSource().getPlayer();
        AtomicInteger numberOfClaimsTotal = new AtomicInteger();

        bumpChat(player);

        server.getWorlds().forEach(world -> {
            RTreeMap<ClaimBox, Claim> worldClaims = GetOffMyLawn.CLAIM.get(world).getClaims();
            int numberOfClaimsWorld = worldClaims.size();
            numberOfClaimsTotal.addAndGet(1);

            player.sendMessage(prefix(new TranslatableText("text.goml.command.number_in", world.getRegistryKey().getValue(), numberOfClaimsWorld)), false);
        });

        player.sendMessage(prefix(new TranslatableText("text.goml.command.number_all", numberOfClaimsTotal.get()).formatted(Formatting.WHITE)), false);

        return 1;
    }

    /**
     * Sends the player information about the claim they are standing in, if it exists.
     *
     * @param context context
     * @return success flag
     */
    private static int infoAdmin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (!world.isClient()) {
            ClaimUtils.getClaimsAt(world, player.getBlockPos()).forEach(claimedArea -> {
                player.sendMessage(prefix(new LiteralText("Origin: " + claimedArea.getValue().getOrigin().toShortString())), false);
                player.sendMessage(prefix(new LiteralText("Radius: " + claimedArea.getValue().getRadius() + " Height: " + claimedArea.getKey().getY())), false);
                {
                    LiteralText owners = new LiteralText("Owners: ");

                    {
                        var iter = claimedArea.getValue().getOwners().iterator();

                        while (iter.hasNext()) {
                            var uuid = iter.next();
                            var gameProfile = context.getSource().getServer().getUserCache().getByUuid(uuid);
                            owners.append(new LiteralText((gameProfile.isPresent() ? gameProfile.get().getName() : "<unknown>") + " -> " + uuid.toString())
                                    .setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    )
                            );

                            if (iter.hasNext()) {
                                owners.append(", ");
                            }
                        }
                    }

                    LiteralText trusted = new LiteralText("Trusted: ");

                    {
                        var iter = claimedArea.getValue().getTrusted().iterator();

                        while (iter.hasNext()) {
                            var uuid = iter.next();
                            var gameProfile = context.getSource().getServer().getUserCache().getByUuid(uuid);
                            trusted.append(new LiteralText((gameProfile.isPresent() ? gameProfile.get().getName() : "<unknown>") + " -> " + uuid.toString())
                                    .setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    )
                            );

                            if (iter.hasNext()) {
                                owners.append(", ");
                            }
                        }
                    }

                    player.sendMessage(prefix(owners), false);
                    player.sendMessage(prefix(trusted), false);
                }
                player.sendMessage(prefix(new LiteralText("ClaimData: ")), false);
                for (var key : (Collection<DataKey<Object>>) (Object) claimedArea.getValue().getDataKeys()) {
                    player.sendMessage(new LiteralText("- " + key.key() + " -> ").append(NbtHelper.toPrettyPrintedText(key.serializer().apply(claimedArea.getValue().getData(key)))), false);
                }

            });
        }

        return 1;
    }

    /**
     * Sends the player general information about all claims in the given world.
     *
     * @param context context
     * @return success flag
     */
    private static int world(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();

        RTreeMap<ClaimBox, Claim> worldClaims = GetOffMyLawn.CLAIM.get(world).getClaims();
        int numberOfClaims = worldClaims.size();

        player.sendMessage(prefix(new TranslatableText("text.goml.command.number_in", world.getRegistryKey().getValue(), numberOfClaims)), false);

        return 1;
    }

    /**
     * Removes the claim the player is currently standing in, if it exists.
     *
     * @param context context
     * @return success flag
     */
    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (!world.isClient()) {
            ClaimUtils.getClaimsAt(world, player.getBlockPos()).forEach(claimedArea -> {
                GetOffMyLawn.CLAIM.get(world).remove(claimedArea.getKey());
                player.sendMessage(prefix(new TranslatableText("text.goml.command.removed_claim", world.getRegistryKey().getValue(), claimedArea.getValue().getOrigin())), false);
                var blockEntity = world.getBlockEntity(claimedArea.getValue().getOrigin(), GOMLEntities.CLAIM_ANCHOR);

                if (blockEntity.isPresent()) {
                    world.breakBlock(claimedArea.getValue().getOrigin(), true);
                    for (var lPos : new ArrayList<>(blockEntity.get().getAugments().keySet())) {
                        world.breakBlock(lPos, true);
                    }
                }

            });
        }

        return 1;
    }

    /**
     * Sends the player information on using the /goml command.
     *
     * @param context context
     * @return success flag
     */
    private static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        Function<String, Text> write = (command) -> new LiteralText("/goml " + command)
                .append(new LiteralText(" - ").formatted(Formatting.GRAY))
                .append(new TranslatableText("text.goml.command.help." + command).setStyle(Style.EMPTY.withColor(0xededed)));

        player.sendMessage(new LiteralText("[").formatted(Formatting.DARK_GRAY).append(new LiteralText("Get Off My Lawn").setStyle(Style.EMPTY.withColor(0xa1ff59))).append("]"), false);
        player.sendMessage(new LiteralText("-------------------------------------").formatted(Formatting.DARK_GRAY), false);

        for (var cmd : context.getSource().getServer().getCommandManager().getDispatcher().findNode(Collections.singleton("goml")).getChildren()) {
            if (cmd.canUse(context.getSource())) {
                player.sendMessage(write.apply(cmd.getName()), false);
            }
        }
        player.sendMessage(new LiteralText("-------------------------------------").formatted(Formatting.DARK_GRAY), false);
        player.sendMessage(new LiteralText("GitHub: ")
                .append(
                        new LiteralText("https://github.com/Patbox/get-off-my-lawn-server-sided")
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Patbox/get-off-my-lawn-server-sided")))
                ), false);

        return 1;
    }

    private static int openList(CommandContext<ServerCommandSource> context, GameProfile target) throws CommandSyntaxException {

        ClaimListGui.open(context.getSource().getPlayer(), target);

        return 1;
    }

    private static int openGui(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var claim = ClaimUtils.getClaimsAt(player.getWorld(), player.getBlockPos());

        if (claim.isEmpty()) {
            player.sendMessage(prefix(new TranslatableText("text.goml.command.no_claims").formatted(Formatting.RED)), false);
            return 0;
        }

        claim.collect(Collectors.toList()).get(0).getValue().openUi(player);

        return 1;
    }

    private static int trust(CommandContext<ServerCommandSource> context, boolean owner) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity toAdd = EntityArgumentType.getPlayer(context, "player");


        if (!world.isClient()) {
            var skipChecks = ClaimUtils.isInAdminMode(player);
            ClaimUtils.getClaimsAt(world, player.getBlockPos()).forEach(claimedArea -> {
                if (skipChecks || claimedArea.getValue().isOwner(player)) {
                    if (owner && !claimedArea.getValue().isOwner(toAdd)) {
                        claimedArea.getValue().addOwner(toAdd);
                        player.sendMessage(prefix(new TranslatableText("text.goml.command.owner_added", toAdd.getDisplayName())), false);
                    } else if (!owner && !claimedArea.getValue().getTrusted().contains(toAdd.getUuid())) {
                        claimedArea.getValue().trust(toAdd);
                        player.sendMessage(prefix(new TranslatableText("text.goml.command.trusted", toAdd.getDisplayName())), false);
                    } else {
                        player.sendMessage(prefix(new TranslatableText("text.goml.command.already_added")), false);
                    }
                }
            });
        }

        return 1;
    }

    private static int untrust(CommandContext<ServerCommandSource> context, boolean owner) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity toRemove = EntityArgumentType.getPlayer(context, "player");

        // Owner/trusted tried to remove themselves from the claim
        if (toRemove.getUuid().equals(player.getUuid()) && !ClaimUtils.isInAdminMode(player)) {
            player.sendMessage(prefix(new TranslatableText("text.goml.command.remove_self")), false);
            return 1;
        }

        ClaimUtils.getClaimsAt(world, player.getBlockPos()).forEach(claimedArea -> {
            if (claimedArea.getValue().isOwner(player)) {
                if (owner) {
                    claimedArea.getValue().getOwners().remove(toRemove.getUuid());
                } else {
                    claimedArea.getValue().untrust(toRemove);
                }
                player.sendMessage(prefix(new TranslatableText("text.goml.command." + (owner ? "owner_removed" : "untrusted"), toRemove.getDisplayName())), false);
            }
        });


        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        GetOffMyLawn.CONFIG = GOMLConfig.loadOrCreateConfig();
        context.getSource().sendFeedback(prefix(new LiteralText("Reloaded config")), false);
        return 1;
    }

    private static void bumpChat(ServerPlayerEntity player) {
        player.sendMessage(new LiteralText(" "), false);
    }

    private static MutableText prefix(MutableText text) {
        return GetOffMyLawn.CONFIG.prefix(text);
    }
}
