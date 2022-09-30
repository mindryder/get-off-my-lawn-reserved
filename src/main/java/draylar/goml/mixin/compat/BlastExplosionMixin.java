package draylar.goml.mixin.compat;

import com.jamieswhiteshirt.rtree3i.Entry;
import com.jamieswhiteshirt.rtree3i.Selection;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
/*
@Pseudo
@Mixin(CustomExplosion.class)
public abstract class BlastExplosionMixin extends Explosion {

    private BlastExplosionMixin(World world, @Nullable Entity entity, double x, double y, double z, float power) {
        super(world, entity, x, y, z, power);
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("TAIL"))
    private void goml_clearBlocks(CallbackInfo ci) {
        ((CustomExplosion) (Object) this).affectedBlocks.removeIf((b) -> !ClaimUtils.canExplosionDestroy(this.world, b, this.getCausingEntity()));
    }

    @ModifyVariable(method = "collectBlocksAndDamageEntities", at = @At("STORE"), ordinal = 0)
    private List<Entity> goml_clearEntities(List<Entity> x) {
        x.removeIf((e) -> !ClaimUtils.canExplosionDestroy(this.world, e.getBlockPos(), this.getCausingEntity()));
        return x;
    }
}*/
