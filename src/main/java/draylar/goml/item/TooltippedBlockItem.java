package draylar.goml.item;

import eu.pb4.polymer.api.block.PolymerHeadBlock;
import eu.pb4.polymer.api.item.PolymerHeadBlockItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class TooltippedBlockItem extends PolymerHeadBlockItem {

    private final int lines;

    public TooltippedBlockItem(PolymerHeadBlock block, Settings settings, int lines) {
        super(block, settings);
        this.lines = lines;
    }

    @Override
    public Text getName() {
        return this.getBlock().getName();
    }

    @Override
    public Text getName(ItemStack stack) {
        return this.getName();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        this.addLines(tooltip::add);
    }

    public void addLines(Consumer<Text> textConsumer) {
        for (int i = 1; i <= lines; i++) {
            textConsumer.accept(new TranslatableText(String.format("%s.description.%d", getTranslationKey(), i)).formatted(Formatting.GRAY));
        }
    }
}
