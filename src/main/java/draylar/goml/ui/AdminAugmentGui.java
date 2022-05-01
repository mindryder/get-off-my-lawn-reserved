package draylar.goml.ui;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class AdminAugmentGui extends SimpleGui {
    private final Claim claim;
    private final Runnable onClose;
    private int claimHeight;
    private int claimRadius;
    private ClaimBox claimBox;


    public AdminAugmentGui(Claim claim, ServerPlayerEntity player, @Nullable Runnable onClose) {
        super(ScreenHandlerType.HOPPER, player, false);
        this.setTitle(new TranslatableText("text.goml.gui.admin_settings.title"));
        this.claim = claim;
        this.onClose = onClose;
        this.claimBox = GetOffMyLawn.CLAIM.get(claim.getWorldInstance(player.server)).getClaims().entries().filter(e -> e.getValue() == this.claim).collect(Collectors.toList()).get(0).getKey();
        this.claimHeight = this.claimBox.radiusY();
        this.claimRadius = this.claimBox.radius();


        this.addSlot(new GuiElementBuilder(Items.STONE_SLAB)
                .setName(new TranslatableText("text.goml.radius", this.claimRadius))
                .setCallback((i, a, c, g) -> {
                    PagedGui.playClickSound(this.player);
                    if (a.isLeft) {
                        this.claimRadius = Math.max(this.claimRadius - (a.shift ? 10 : 1), 1);
                    } else if (a.isRight) {
                        this.claimRadius += a.shift ? 10 : 1;
                    }
                    g.getSlot(i).getItemStack().setCustomName(new TranslatableText("text.goml.radius", this.claimRadius).setStyle(Style.EMPTY.withItalic(false)));
                })
        );
        this.addSlot(new GuiElementBuilder(Items.ANDESITE_WALL)
                .setName(new TranslatableText("text.goml.height", this.claimHeight))
                .setCallback((i, a, c, g) -> {
                    PagedGui.playClickSound(this.player);
                    if (a.isLeft) {
                        this.claimHeight = Math.max(this.claimHeight - (a.shift ? 10 : 1), 1);
                    } else if (a.isRight) {
                        this.claimHeight += a.shift ? 10 : 1;
                    }
                    g.getSlot(i).getItemStack().setCustomName(new TranslatableText("text.goml.height", this.claimHeight).setStyle(Style.EMPTY.withItalic(false)));

                })
        );

        this.addSlot(new GuiElementBuilder(Items.SLIME_BALL)
                .setName(new TranslatableText("text.goml.apply"))
                .setCallback((i, a, c, g) -> {
                    PagedGui.playClickSound(this.player);
                    GetOffMyLawn.CLAIM.get(claim.getWorldInstance(player.server)).remove(this.claimBox);
                    this.claimBox = new ClaimBox(this.claim.getOrigin(), this.claimRadius, this.claimHeight);
                    GetOffMyLawn.CLAIM.get(claim.getWorldInstance(player.server)).add(this.claimBox, this.claim);
                    claim.internal_setClaimBox(this.claimBox);
                })
        );

        this.open();
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }
}
