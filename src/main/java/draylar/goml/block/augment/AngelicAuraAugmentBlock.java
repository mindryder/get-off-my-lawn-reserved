package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.block.ClaimAugmentBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class AngelicAuraAugmentBlock extends ClaimAugmentBlock {

    public AngelicAuraAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        if (player.age % 80 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 80, 0, true, false));
        }
    }

    @Override
    public boolean ticks() {
        return true;
    }
}
