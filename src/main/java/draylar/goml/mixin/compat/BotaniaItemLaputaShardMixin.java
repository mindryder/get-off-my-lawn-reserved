package draylar.goml.mixin.compat;

import draylar.goml.api.ClaimUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.common.item.ItemLaputaShard;

@Unique
@Mixin(ItemLaputaShard.class)
public class BotaniaItemLaputaShardMixin {
    @Redirect(method = {"spawnNextBurst", "updateBurst"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState goml_canReplace(World instance, BlockPos pos) {
        if (ClaimUtils.getClaimsAt(instance, pos).isNotEmpty()) {
            return Blocks.BEDROCK.getDefaultState();
        }

        return instance.getBlockState(pos);
    }

}
