package draylar.goml.block.augment;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.DataKey;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.registry.GOMLEntities;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GreeterAugmentBlock extends ClaimAugmentBlock {

    public static final DataKey<String> MESSAGE_KEY = DataKey.ofString("goml:greeter/message", "Welcome %player on my claim!");

    public GreeterAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
    }

    @Override
    public boolean ticks() {
        return false;
    }


    @Override
    public void onPlayerEnter(Claim claim, PlayerEntity player) {
        var text = claim.getData(MESSAGE_KEY);

        if (text != null && !text.isBlank()) {
            player.sendMessage(new LiteralText("[Claim] ").formatted(Formatting.GRAY).append(new LiteralText(text.replace("%player", player.getName().getString())).formatted(Formatting.WHITE)), false);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit) {
        if (playerEntity instanceof ServerPlayerEntity player) {
            var blockEntity = world.getBlockEntity(pos, GOMLEntities.CLAIM_AUGMENT);

            if (blockEntity.isPresent() && blockEntity.get().getParent() != null) {
                var claim = blockEntity.get().getParent().getClaim();

                if (claim != null && claim.isOwner(player)) {
                    var currentInput = claim.getData(MESSAGE_KEY);

                    var ui = new AnvilInputGui(player, false);
                    ui.setTitle(new TranslatableText("text.goml.gui.input_greeting.title"));
                    ui.setDefaultInputValue(currentInput);

                    ui.setSlot(1,
                            new GuiElementBuilder(Items.BARRIER)
                                    .setName(new TranslatableText("text.goml.gui.input_greeting.close").formatted(Formatting.RED))

                                    .setCallback((index, clickType, actionType) -> {
                                        ui.close();
                                    })
                    );

                    ui.setSlot(2,
                            new GuiElementBuilder(Items.SLIME_BALL)
                                    .setName(new TranslatableText("text.goml.gui.input_greeting.set").formatted(Formatting.GREEN))

                                    .setCallback((index, clickType, actionType) -> {
                                        claim.setData(MESSAGE_KEY, ui.getInput());
                                        player.sendMessage(new TranslatableText("text.goml.changed_greeting", new LiteralText(ui.getInput()).formatted(Formatting.WHITE)).formatted(Formatting.GREEN), false);
                                    })
                    );

                    ui.open();

                }

                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, playerEntity, hand, hit);
    }
}
