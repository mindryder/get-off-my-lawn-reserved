package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.block.SelectiveClaimAugmentBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class WitheringSealAugmentBlock extends SelectiveClaimAugmentBlock {

    public WitheringSealAugmentBlock(Settings settings, String texture) {
        super("withering_seal", settings, texture);
    }

    @Override
    public boolean ticks() {
        return true;
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        if (canApply(claim, player)) {
            player.removeStatusEffect(StatusEffects.WITHER);
        }
    }
}
