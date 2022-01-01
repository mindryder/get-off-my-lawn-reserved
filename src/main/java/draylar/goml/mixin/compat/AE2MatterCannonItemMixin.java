package draylar.goml.mixin.compat;

import appeng.api.util.DimensionalBlockPos;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.Platform;
import draylar.goml.api.ClaimUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MatterCannonItem.class)
public class AE2MatterCannonItemMixin {
    @Unique
    private static PlayerEntity goml_player;

    @Inject(method = "standardAmmo", at = @At("HEAD"))
    private void goml_catchPlayer(float f1, World boundingBox, PlayerEntity player, Vec3d entity1, Vec3d err, Vec3d el, double entityHit, double dmg, double entityResult, CallbackInfo ci) {
        this.goml_player = player;
    }

    @Inject(method = "standardAmmo", at = @At("RETURN"))
    private void goml_clearPlayer(float f1, World boundingBox, PlayerEntity player, Vec3d entity1, Vec3d err, Vec3d el, double entityHit, double dmg, double entityResult, CallbackInfo ci) {
        this.goml_player = null;
    }

    @Inject(method = "shootPaintBalls", at = @At("HEAD"))
    private void goml_catchPlayer2(ItemStack f1, World boundingBox, PlayerEntity player, Vec3d entity1, Vec3d err, Vec3d sh, double entityHit, double entityResult, double hp, CallbackInfo ci) {
        this.goml_player = player;
    }

    @Inject(method = "shootPaintBalls", at = @At("RETURN"))
    private void goml_clearPlayer2(ItemStack f1, World boundingBox, PlayerEntity player, Vec3d entity1, Vec3d err, Vec3d sh, double entityHit, double entityResult, double hp, CallbackInfo ci) {
        this.goml_player = null;
    }

    @Inject(method = "lambda$standardAmmo$1", at = @At("HEAD"), cancellable = true)
    private static void goml_skipClaimedEntities(Entity e, CallbackInfoReturnable<Boolean> cir) {
        if (!ClaimUtils.canModify(e.getEntityWorld(), e.getBlockPos(), goml_player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "lambda$shootPaintBalls$0", at = @At("HEAD"), cancellable = true)
    private static void goml_skipClaimedEntities2(Entity e, CallbackInfoReturnable<Boolean> cir) {
        if (!ClaimUtils.canModify(e.getEntityWorld(), e.getBlockPos(), goml_player)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "standardAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private static boolean goml_protectBlock(World instance, BlockPos pos, boolean b) {
        if (!ClaimUtils.canModify(instance, pos, goml_player)) {
            return false;
        }
        return instance.breakBlock(pos, b);
    }

    @Redirect(method = "shootPaintBalls", at = @At(value = "INVOKE", target = "Lappeng/util/Platform;hasPermissions(Lappeng/api/util/DimensionalBlockPos;Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static boolean goml_protectBlock2(DimensionalBlockPos dc, PlayerEntity player) {
        if (!ClaimUtils.canModify(player.world, dc.getPos(), goml_player)) {
            return false;
        }
        return Platform.hasPermissions(dc, player);
    }
}
