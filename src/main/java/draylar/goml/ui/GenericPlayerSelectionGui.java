package draylar.goml.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GenericPlayerSelectionGui extends PagedGui {
    private final PlayerManager playerManager;
    private final Predicate<ServerPlayerEntity> shouldDisplay;
    private final Consumer<ServerPlayerEntity> onClick;
    private int ticker;
    private List<ServerPlayerEntity> cachedPlayers = Collections.emptyList();

    public GenericPlayerSelectionGui(ServerPlayerEntity player, Text title, Predicate<ServerPlayerEntity> shouldDisplay, Consumer<ServerPlayerEntity> onClick, Runnable postClose) {
        super(player, postClose);
        this.shouldDisplay = shouldDisplay;
        this.onClick = onClick;
        this.playerManager = Objects.requireNonNull(player.getServer()).getPlayerManager();
        this.setTitle(title);
        this.updateDisplay();
        this.open();
    }

    @Override
    protected int getPageAmount() {
        return playerManager.getCurrentPlayerCount() / PAGE_SIZE;
    }

    @Override
    protected void updateDisplay() {
        List<ServerPlayerEntity> list = new ArrayList<>();
        for (ServerPlayerEntity p : this.playerManager.getPlayerList()) {
            if (this.shouldDisplay.test(p)) {
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
                       this.onClick.accept(player);
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
