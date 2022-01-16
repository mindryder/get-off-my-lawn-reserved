package draylar.goml.mixin;

import draylar.goml.other.AdminModePlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements AdminModePlayer {

    @Unique
    private boolean goml_adminMode = false;

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void goml_copyAdminMode(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.goml_adminMode = ((AdminModePlayer) oldPlayer).goml_getAdminMode();
    }

    @Override
    public void goml_setAdminMode(boolean value) {
        this.goml_adminMode = value;
    }

    @Override
    public boolean goml_getAdminMode() {
        return this.goml_adminMode;
    }
}
