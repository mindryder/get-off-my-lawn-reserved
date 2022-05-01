package draylar.goml.block.augment;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.DataKey;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.other.StatusEnum;
import draylar.goml.ui.PagedGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class ExplosionControllerAugmentBlock extends ClaimAugmentBlock {
    public static final DataKey<StatusEnum.Toggle> KEY = DataKey.ofEnum(GetOffMyLawn.id("explosion_control"), StatusEnum.Toggle.class, StatusEnum.Toggle.ENABLED);

    public ExplosionControllerAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
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

        gui.setTitle(this.getGuiName());

        var change = new MutableObject<Runnable>();
        change.setValue(() -> {
            var currentMode = claim.getData(KEY);
            gui.setSlot(0, new GuiElementBuilder(currentMode.getIcon())
                    .setName(new TranslatableText("text.goml.explosion_control_toggle", currentMode.getName()))
                    .addLoreLine(new TranslatableText("text.goml.mode_toggle.help").formatted(Formatting.GRAY))
                    .setCallback((x, y, z) -> {
                        PagedGui.playClickSound(player);
                        var mode = currentMode.getNext();
                        claim.setData(KEY, mode);
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
}
