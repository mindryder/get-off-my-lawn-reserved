package draylar.goml.mixin.compat;

import draylar.goml.api.ClaimUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.entity.EntityManaBurst;

@Pseudo
@Mixin(EntityManaBurst.class)
public abstract class BotaniaEntityManaBurstMixin extends ThrownEntity {
    protected BotaniaEntityManaBurstMixin(EntityType<? extends ThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void goml_protectClaims(BlockHitResult receiver, CallbackInfo ci) {
        if (this.getOwner() instanceof PlayerEntity player && !ClaimUtils.canModify(this.getWorld(), receiver.getBlockPos(), player)) {
            this.discard();
            ci.cancel();
        }
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void goml_protectEntities(EntityHitResult hit, CallbackInfo ci) {
        if (this.getOwner() instanceof PlayerEntity player && !ClaimUtils.canModify(this.getWorld(), new BlockPos(hit.getPos()), player)) {
            this.discard();
            ci.cancel();
        }
    }
}
