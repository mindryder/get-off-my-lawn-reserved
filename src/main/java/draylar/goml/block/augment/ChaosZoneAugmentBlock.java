package draylar.goml.block.augment;

import draylar.goml.api.Claim;
import draylar.goml.block.ClaimAugmentBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class ChaosZoneAugmentBlock extends ClaimAugmentBlock {

    public ChaosZoneAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
    }

    @Override
    public boolean ticks() {
        return true;
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 5, 0, true, false));
    }
}
