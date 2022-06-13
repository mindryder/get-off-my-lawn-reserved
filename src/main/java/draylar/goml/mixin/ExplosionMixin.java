package draylar.goml.mixin;

import draylar.goml.api.ClaimUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Shadow @Final public World world;

    @Shadow @Nullable public abstract LivingEntity getCausingEntity();

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("TAIL"))
    private void goml_clearBlocks(CallbackInfo ci) {
        this.affectedBlocks.removeIf((b) -> !ClaimUtils.canExplosionDestroy(this.world, b, this.getCausingEntity()));
    }

    @ModifyVariable(method = "collectBlocksAndDamageEntities", at = @At("STORE"), ordinal = 0)
    private List<Entity> goml_clearEntities(List<Entity> x) {
        x.removeIf((e) -> !ClaimUtils.canExplosionDestroy(this.world, e.getBlockPos(), this.getCausingEntity()));
        return x;
    }
}
