package draylar.goml.ui;

import com.mojang.authlib.GameProfile;
import draylar.goml.registry.GOMLTextures;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Internal
public class GenericPlayerSelectionGui extends PagedGui {
    private final PlayerManager playerManager;
    private final Predicate<GameProfile> shouldDisplay;
    private final Consumer<GameProfile> onClick;
    private int ticker;
    private List<GameProfile> cachedPlayers = Collections.emptyList();

    public GenericPlayerSelectionGui(ServerPlayerEntity player, Text title, Predicate<GameProfile> shouldDisplay, Consumer<GameProfile> onClick, Runnable postClose) {
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
        List<GameProfile> list = new ArrayList<>();
        for (var p : this.playerManager.getPlayerList()) {
            if (this.shouldDisplay.test(p.getGameProfile())) {
                list.add(p.getGameProfile());
            }
        }
        list.sort(Comparator.comparing((player) -> player.getName()));
        this.cachedPlayers = list;
        super.updateDisplay();
    }

    @Override
    protected DisplayElement getElement(int id) {
       if (this.cachedPlayers.size() > id) {
           var player = this.cachedPlayers.get(id);
           return DisplayElement.of(
                   new GuiElementBuilder(Items.PLAYER_HEAD)
                           .setName(Text.literal(player.getName()))
                           .setSkullOwner(player, null)
                           .setCallback((x, y, z) -> {
                       playClickSound(this.player);
                       this.onClick.accept(player);
                       this.close(this.closeCallback != null);
                   })
           );
       }

        return DisplayElement.empty();
    }

    @Override
    protected DisplayElement getNavElement(int id) {
        return switch (id) {
            case 5 -> DisplayElement.of(new GuiElementBuilder(Items.NAME_TAG)
                    .setName(Text.translatable("text.goml.gui.player_selector.by_name").formatted(Formatting.GREEN))
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);

                        this.ignoreCloseCallback = true;
                        this.close(true);
                        this.ignoreCloseCallback = false;
                        new NamePlayerSelectorGui(this.player, this.shouldDisplay, this::refreshOpen, (p) -> {
                            this.onClick.accept(p);
                            this.close(true);
                            if (this.closeCallback != null) {
                                this.closeCallback.run();
                            }
                        });
                    }));
            default -> super.getNavElement(id);
        };
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
