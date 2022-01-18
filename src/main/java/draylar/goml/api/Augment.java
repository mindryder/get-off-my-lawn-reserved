package draylar.goml.api;

import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.block.entity.ClaimAugmentBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Defines behavior for a claim Augment, which handles events for players inside claims.
 */
public interface Augment {

    default void onPlayerEnter(Claim claim, PlayerEntity player) {

    }

    default void onPlayerExit(Claim claim, PlayerEntity player) {

    }

    default void tick(Claim claim, World world) {

    }

    default void playerTick(Claim claim, PlayerEntity player) {

    }

    default boolean ticks() {
        return false;
    }

    default boolean canPlace(Claim claim, World world, BlockPos pos, ClaimAnchorBlockEntity anchor) {
        return true;
    }

    default boolean hasSettings() {
        return false;
    }

    default void openSettings(Claim claim, ServerPlayerEntity player, @Nullable Runnable closeCallback) {}

    default boolean isEnabled(Claim claim, World world) {
        return true;
    }

    default Text getAugmentName() {
        return new LiteralText("<unknown>");
    }
}
