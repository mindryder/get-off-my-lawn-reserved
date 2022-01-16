package draylar.goml.ui;

import com.jamieswhiteshirt.rtree3i.Entry;
import com.mojang.authlib.GameProfile;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClaimListGui extends PagedGui {

    private final List<Entry<ClaimBox, Claim>> claimList = new ArrayList<>();

    public static void open(ServerPlayerEntity player, GameProfile target) {
        new ClaimListGui(player, target).open();
    }

    protected ClaimListGui(ServerPlayerEntity player, GameProfile target) {
        super(player);

        ClaimUtils.getClaimsWithAccess(player.getWorld(), target.getId()).forEach(this.claimList::add);
        this.setTitle(new TranslatableText(
                player.getGameProfile().getId().equals(target.getId()) ? "text.goml.your_claims" : "text.goml.someones_claims",
                target.getName()
        ));

        this.updateDisplay();
    }

    @Override
    protected int getPageAmount() {
        return this.claimList.size() / PAGE_SIZE + 1;
    }

    @Override
    protected DisplayElement getElement(int id) {
        if (this.claimList.size() > id) {
            var server = this.player.getServer();
            var entry = this.claimList.get(id);
            var claim = entry.getValue();

            var icon = GuiElementBuilder.from(claim.getIcon());
            icon.setName(new LiteralText(claim.getOrigin().toShortString()).append(new LiteralText(" (" + claim.getWorld().toString() + ")").formatted(Formatting.GRAY)));

            var owners = getPlayerNames(server, claim.getOwners());
            var trusted = getPlayerNames(server, claim.getTrusted());

            icon.setLore(new ArrayList<>());

            icon.addLoreLine(new TranslatableText("text.goml.radius",
                    new LiteralText("" + entry.getKey().getRadius()).formatted(Formatting.WHITE)
            ).formatted(Formatting.YELLOW));

            if (!owners.isEmpty()) {
                icon.addLoreLine(new TranslatableText("text.goml.owners", owners.remove(0)).formatted(Formatting.GOLD));

                for (var text : owners) {
                    icon.addLoreLine(new LiteralText("   ").append(text));
                }
            }

            if (!trusted.isEmpty()) {
                icon.addLoreLine(new TranslatableText("text.goml.trusted", trusted.remove(0)).formatted(Formatting.GREEN));

                for (var text : trusted) {
                    icon.addLoreLine(new LiteralText("   ").append(text));
                }
            }

            icon.setCallback((x, y, z) -> {
               if (Permissions.check(this.player, "goml.teleport", 3)) {
                   var world = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, claim.getWorld()));
                   if (world != null) {
                       this.player.teleport(world, claim.getOrigin().getX(), claim.getOrigin().getY() + 1, claim.getOrigin().getZ(), this.player.getYaw(), this.player.getPitch());
                   }
               }
            });

            return DisplayElement.of(icon);
        }

        return DisplayElement.empty();
    }

    public static final List<Text> getPlayerNames(MinecraftServer server, Collection<UUID> uuids) {
        var list = new ArrayList<Text>();

        var builder = new StringBuilder();
        var iterator = uuids.iterator();
        while (iterator.hasNext()) {
            var gameProfile = server.getUserCache().getByUuid(iterator.next());
            if (gameProfile.isPresent()) {
                builder.append(gameProfile.get().getName());

                if (iterator.hasNext()) {
                    builder.append(", ");
                }

                if (builder.length() > 32) {
                    list.add(new LiteralText(builder.toString()).formatted(Formatting.WHITE));
                    builder = new StringBuilder();
                }
            }
        }
        if (!builder.isEmpty()) {
            list.add(new LiteralText(builder.toString()).formatted(Formatting.WHITE));
        }

        return list;
    }
}
