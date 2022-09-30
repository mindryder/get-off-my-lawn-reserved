package draylar.goml.mixin.augment;

import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.registry.GOMLBlocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity {

    private EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "teleportTo(DDD)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goml$attemptTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        boolean b = ClaimUtils.getClaimsAt(this.world, this.getBlockPos())
                .anyMatch(claim -> claim.getValue().hasAugment(GOMLBlocks.ENDER_BINDING.getFirst()));

        if (b) {
            cir.setReturnValue(false);
        }
    }

    @Mixin(targets = {"net/minecraft/entity/mob/EndermanEntity$PlaceBlockGoal"})
    public static abstract class PlaceBlockGoalMixin extends Goal {
        @Shadow @Final private EndermanEntity enderman;

        @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
        private void goml$cancelInClaim(CallbackInfoReturnable<Boolean> cir) {
            boolean b = ClaimUtils.getClaimsAt(this.enderman.world, this.enderman.getBlockPos())
                    .anyMatch(claim -> claim.getValue().hasAugment(GOMLBlocks.ENDER_BINDING.getFirst()));

            if (b) {
                cir.setReturnValue(false);
            }
        }
    }

    @Mixin(targets = {"net/minecraft/entity/mob/EndermanEntity$PickUpBlockGoal"})
    public static abstract class PickBlockGoalMixin extends Goal {
        @Shadow @Final private EndermanEntity enderman;

        @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
        private void goml$cancelInClaim(CallbackInfoReturnable<Boolean> cir) {
            boolean b = ClaimUtils.getClaimsAt(this.enderman.world, this.enderman.getBlockPos())
                    .anyMatch(claim -> claim.getValue().hasAugment(GOMLBlocks.ENDER_BINDING.getFirst()));

            if (b) {
                cir.setReturnValue(false);
            }
        }
    }
}
