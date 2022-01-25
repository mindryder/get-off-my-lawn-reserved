package draylar.goml.mixin.compat;

import appeng.entity.TinyTNTPrimedEntity;
import draylar.goml.api.ClaimUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(TinyTNTPrimedEntity.class)
public abstract class AE2TinyTNTEntityMixin extends TntEntity {
    @Shadow @Nullable
    public abstract LivingEntity getCausingEntity();

    public AE2TinyTNTEntityMixin(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "method_6971", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean goml_damageEntities(Entity instance, DamageSource source, float amount) {
        if (ClaimUtils.canExplosionDestroy(this.world, instance.getBlockPos(), this.getCausingEntity())) {
            return instance.damage(source, amount);
        }

        return false;
    }

    @Redirect(method = "method_6971", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState goml_damageBlocks(World instance, BlockPos pos) {
        if (ClaimUtils.canExplosionDestroy(this.world, pos, this.getCausingEntity())) {
            return Blocks.AIR.getDefaultState();
        }

        return instance.getBlockState(pos);
    }
}
