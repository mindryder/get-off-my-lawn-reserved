package draylar.goml.ui;

import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.registry.GOMLTextures;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

public class ClaimPlayerListGui extends PagedGui {
    private final Claim claim;
    private final boolean canModifyTrusted;
    private final boolean canModifyOwners;
    private final boolean isAdmin;

    private List<UUID> cachedOwners = Collections.emptyList();
    private List<UUID> cachedTrusted = Collections.emptyList();

    public ClaimPlayerListGui(ServerPlayerEntity player, Claim claim, boolean canModifyTrusted, boolean canModifyOwners, boolean isAdmin) {
        super(player);
        this.claim = claim;
        this.canModifyOwners = canModifyOwners;
        this.canModifyTrusted = canModifyTrusted;
        this.isAdmin = isAdmin;
        this.setTitle(new TranslatableText("text.goml.gui.player_list.title"));
        this.updateDisplay();
        this.open();
    }

    public static void open(ServerPlayerEntity player, Claim claim, boolean admin) {
        new ClaimPlayerListGui(player, claim, claim.isOwner(player) || admin, admin, admin);
    }

    @Override
    protected int getPageAmount() {
        return (this.cachedOwners.size() + this.cachedTrusted.size()) / PAGE_SIZE;
    }

    @Override
    protected void updateDisplay() {
        this.cachedOwners = new ArrayList<>(this.claim.getOwners());
        this.cachedTrusted = new ArrayList<>(this.claim.getTrusted());
        super.updateDisplay();
    }

    @Override
    protected DisplayElement getElement(int id) {
        var ownerSize = this.cachedOwners.size();
        var trustSize = this.cachedTrusted.size();
        if (ownerSize > id) {
            return getPlayerElement(this.cachedOwners.get(id), true);
        } else if (trustSize > id - ownerSize) {
            return getPlayerElement(this.cachedTrusted.get(id - ownerSize), false);

        }

        return DisplayElement.empty();
    }

    @Override
    protected DisplayElement getNavElement(int id) {
        if (id == 4 && this.canModifyTrusted) {
            return DisplayElement.of(new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(new TranslatableText("text.goml.gui.player_list.add_player").formatted(Formatting.GREEN))
                    .setSkullOwner(GOMLTextures.GUI_ADD)
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);

                        new PlayerAddGui(this.player, this.claim, () -> open(player, this.claim, this.isAdmin));
                    })
            );
        }
        return super.getNavElement(id);
    }

    private DisplayElement getPlayerElement(UUID uuid, boolean owner) {
        var optional = this.player.server.getUserCache().getByUuid(uuid);
        var gameProfile = optional.get();
        var exist = gameProfile != null;

        var canRemove = owner ? this.canModifyOwners : this.canModifyTrusted;

        var builder = new GuiElementBuilder(exist ? Items.PLAYER_HEAD : Items.SKELETON_SKULL)
                .setName(new LiteralText(exist ? gameProfile.getName() : uuid.toString())
                        .formatted(owner ? Formatting.GOLD : Formatting.WHITE)
                        .append(owner
                                        ? new LiteralText(" (").formatted(Formatting.DARK_GRAY)
                                                .append(new TranslatableText("text.goml.owner").formatted(Formatting.WHITE))
                                                .append(new LiteralText(")").formatted(Formatting.DARK_GRAY))

                                        : LiteralText.EMPTY
                                )
                );

        if (canRemove) {
            builder.addLoreLine(new TranslatableText("text.goml.gui.click_to_remove"));
            builder.setCallback((x, y, z) -> {
                playClickSound(player);
                (owner ? this.claim.getOwners() : this.claim.getTrusted()).remove(uuid);
                this.updateDisplay();
            });
        }

        if (exist) {
            builder.setSkullOwner(gameProfile, null);
        }


        return DisplayElement.of(
                builder
        );
    }
}
