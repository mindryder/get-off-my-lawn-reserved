package draylar.goml.item;

import eu.pb4.polymer.api.block.PolymerHeadBlock;
import eu.pb4.polymer.api.item.PolymerHeadBlockItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ToggleableBlockItem extends TooltippedBlockItem {
    private final BooleanSupplier isEnabled;

    public ToggleableBlockItem(PolymerHeadBlock block, Settings settings, int lines, BooleanSupplier isEnabled) {
        super(block, settings, lines);
        this.isEnabled = isEnabled;
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return isEnabled.getAsBoolean() ? super.canPlace(context, state) : false;
    }

    public boolean isEnabled() {
        return this.isEnabled.getAsBoolean();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (isEnabled.getAsBoolean()) {
            super.appendTooltip(stack, world, tooltip, context);
        } else {
            tooltip.add(new TranslatableText(String.format("text.goml.disabled_augment")).formatted(Formatting.RED, Formatting.BOLD));
        }
    }
}
