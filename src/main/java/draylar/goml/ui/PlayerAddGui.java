package draylar.goml.ui;

import draylar.goml.api.Claim;
import draylar.goml.registry.GOMLTextures;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerAddGui extends PagedGui {
    private final Claim claim;
    private final PlayerManager playerManager;
    private final Runnable postClose;
    private int ticker;
    private List<ServerPlayerEntity> cachedPlayers = Collections.emptyList();

    public PlayerAddGui(ServerPlayerEntity player, Claim claim, Runnable postClose) {
        super(player);
        this.claim = claim;
        this.postClose = postClose;
        this.playerManager = player.getServer().getPlayerManager();
        this.setTitle(new TranslatableText("text.goml.gui.player_add_gui.title"));
        this.updateDisplay();
        this.open();
    }

    @Override
    public void onClose() {
        this.postClose.run();
        super.onClose();
    }

    @Override
    protected int getPageAmount() {
        return playerManager.getCurrentPlayerCount() / PAGE_SIZE;
    }

    @Override
    protected void updateDisplay() {
        List<ServerPlayerEntity> list = new ArrayList<>();
        for (ServerPlayerEntity p : this.playerManager.getPlayerList()) {
            if (!this.claim.hasPermission(p)) {
                list.add(p);
            }
        }
        list.sort(Comparator.comparing((player) -> player.getGameProfile().getName()));
        this.cachedPlayers = list;
        super.updateDisplay();
    }

    @Override
    protected DisplayElement getElement(int id) {
       if (this.cachedPlayers.size() > id) {
           var player = this.cachedPlayers.get(id);
           return DisplayElement.of(
                   new GuiElementBuilder(Items.PLAYER_HEAD)
                           .setName(player.getName())
                           .setSkullOwner(player.getGameProfile(), null)
                           .setCallback((x, y, z) -> {
                       playClickSound(this.player);
                       this.claim.trust(player);
                       this.close();
                   })
           );
       }

        return DisplayElement.empty();
    }

    @Override
    public void onTick() {
        this.ticker++;
        if (this.ticker == 20) {
            this.ticker = 0;
            this.updateDisplay();
        }
        super.onTick();
    }
}
