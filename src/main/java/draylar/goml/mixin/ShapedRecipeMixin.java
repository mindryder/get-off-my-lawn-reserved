package draylar.goml.mixin;

import draylar.goml.item.ToggleableBlockItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Shadow @Final private ItemStack output;

    @Inject(method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    private void goml_cancelIfDisabled(CraftingInventory craftingInventory, World world, CallbackInfoReturnable<Boolean> cir) {
        if (this.output.getItem() instanceof ToggleableBlockItem item && !item.isEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
