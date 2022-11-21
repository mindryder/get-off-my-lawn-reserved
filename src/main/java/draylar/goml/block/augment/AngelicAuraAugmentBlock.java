package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.block.SelectiveClaimAugmentBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class AngelicAuraAugmentBlock extends SelectiveClaimAugmentBlock {

    public AngelicAuraAugmentBlock(Settings settings, String texture) {
        super("angelic_aura", settings, texture);
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        if (player.age % 80 == 0 && canApply(claim, player)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 80, 0, true, false));
        }
    }

    @Override
    public boolean ticks() {
        return true;
    }
}
