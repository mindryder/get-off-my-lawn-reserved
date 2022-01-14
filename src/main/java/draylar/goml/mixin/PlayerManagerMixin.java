package draylar.goml.mixin;

import draylar.goml.block.augment.HeavenWingsAugmentBlock;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void goml_remove(ServerPlayerEntity player, CallbackInfo ci) {
        HeavenWingsAugmentBlock.HEAVEN_WINGS.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
    }
}
