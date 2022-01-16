package draylar.goml.api;

import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.block.entity.ClaimAugmentBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Defines behavior for a claim Augment, which handles events for players inside claims.
 */
public interface Augment {

    default void onPlayerEnter(Claim claim, PlayerEntity player) {

    }

    default void onPlayerExit(Claim claim, PlayerEntity player) {

    }

    default void tick(Claim claim, World world, ClaimAugmentBlockEntity be) {

    }

    default void playerTick(Claim claim, PlayerEntity player) {

    }

    default boolean ticks() {
        return false;
    }

    default boolean canPlace(Claim claim, World world, BlockPos pos, ClaimAnchorBlockEntity anchor) {
        return true;
    }

    default boolean isEnabled(Claim claim, World world) {
        return true;
    }
}
