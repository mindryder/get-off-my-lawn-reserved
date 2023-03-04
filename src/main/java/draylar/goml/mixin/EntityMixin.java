package draylar.goml.mixin;

import draylar.goml.api.ClaimUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow public World world;

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void goml$isInvulnerable(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!ClaimUtils.canDamageEntity(this.world, (Entity) (Object) this, damageSource)) {
            cir.setReturnValue(true);
        }
    }
}
