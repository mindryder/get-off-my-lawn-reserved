package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.DataKey;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.registry.GOMLTextures;
import draylar.goml.ui.ClaimAugmentGui;
import draylar.goml.ui.ClaimPlayerListGui;
import draylar.goml.ui.PagedGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import javax.xml.crypto.Data;
import java.util.Locale;

public class HeavenWingsAugmentBlock extends ClaimAugmentBlock {

    public static final AbilitySource HEAVEN_WINGS = Pal.getAbilitySource("goml", "heaven_wings");

    public static final DataKey<Mode> MODE_KEY = DataKey.ofEnum("goml:heaven_wings/mode", Mode.class, Mode.EVERYONE);

    public HeavenWingsAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
    }

    @Override
    public void onPlayerEnter(Claim claim, PlayerEntity player) {
        var mode = claim.getData(MODE_KEY);
        if (mode == Mode.EVERYONE || (mode == Mode.TRUSTED && claim.hasPermission(player))) {
            HEAVEN_WINGS.grantTo(player, VanillaAbilities.ALLOW_FLYING);
        }
    }

    @Override
    public void onPlayerExit(Claim claim, PlayerEntity player) {
        HEAVEN_WINGS.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public void openSettings(Claim claim, ServerPlayerEntity player, @Nullable Runnable closeCallback) {

        var gui = new SimpleGui(ScreenHandlerType.HOPPER, player, false) {
            @Override
            public void onClose() {
                if (closeCallback != null) {
                    closeCallback.run();
                }
            }
        };

        gui.setTitle(new TranslatableText("text.goml.gui.heaven_wings.title"));

        var change = new MutableObject<Runnable>();
        change.setValue(() -> {
            var currentMode = claim.getData(MODE_KEY);
            gui.setSlot(0, new GuiElementBuilder(currentMode.getIcon())
                    .setName(new TranslatableText("text.goml.gui.heaven_wings.toggle", currentMode.getName()))
                    .addLoreLine(new TranslatableText("text.goml.gui.heaven_wings.toggle.help").formatted(Formatting.GRAY))
                    .setCallback((x, y, z) -> {
                        PagedGui.playClickSound(player);
                        var mode = currentMode.getNext();
                        claim.setData(MODE_KEY, mode);
                        for (var p : claim.getPlayersIn(player.server)) {
                            if (mode == Mode.EVERYONE || (mode == Mode.TRUSTED && claim.hasPermission(p))) {
                                HEAVEN_WINGS.grantTo(p, VanillaAbilities.ALLOW_FLYING);
                            } else {
                                HEAVEN_WINGS.revokeFrom(p, VanillaAbilities.ALLOW_FLYING);
                            }
                        }
                        change.getValue().run();
                    })
            );
        });

        change.getValue().run();

        gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(new TranslatableText(closeCallback != null ? "text.goml.gui.back" : "text.goml.gui.close").formatted(Formatting.RED))
                .setCallback((x, y, z) -> {
                    PagedGui.playClickSound(player);
                    gui.close();
                })
        );

        while (gui.getFirstEmptySlot() != -1) {
            gui.addSlot(PagedGui.DisplayElement.filler().element());
        }

        gui.open();
    }

    public enum Mode {
        EVERYONE,
        TRUSTED,
        DISABLED;

        public Item getIcon() {
            return switch (this) {
                case EVERYONE -> Items.GREEN_WOOL;
                case TRUSTED -> Items.YELLOW_WOOL;
                case DISABLED -> Items.GRAY_WOOL;
            };
        }

        public Mode getNext() {
            return switch (this) {
                case EVERYONE -> TRUSTED;
                case TRUSTED -> DISABLED;
                case DISABLED -> EVERYONE;
            };
        }

        public Mode getPrevious() {
            return switch (this) {
                case EVERYONE -> DISABLED;
                case TRUSTED -> EVERYONE;
                case DISABLED -> TRUSTED;
            };
        }

        public Text getName() {
            return new TranslatableText("text.goml.gui.heaven_wings.mode." + this.name().toLowerCase(Locale.ROOT));
        }
    }
}
