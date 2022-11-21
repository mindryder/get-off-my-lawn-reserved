package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.block.SelectiveClaimAugmentBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class LakeSpiritGraceAugmentBlock extends SelectiveClaimAugmentBlock {

    public LakeSpiritGraceAugmentBlock(Settings settings, String texture) {
        super("lake_spirit", settings, texture);
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        if (this.canApply(claim, player)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 5, 0, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 5, 0, true, false));
        }
    }

    @Override
    public boolean ticks() {
        return true;
    }
}
