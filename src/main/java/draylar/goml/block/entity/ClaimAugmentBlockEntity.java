package draylar.goml.block.entity;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Augment;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.registry.GOMLEntities;
import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class ClaimAugmentBlockEntity extends BlockEntity implements PolymerObject {

    private static final String PARENT_POSITION_KEY = "ParentPosition";
    private BlockPos parentPosition;
    private Augment augment;
    @Nullable
    private Claim claim;

    public ClaimAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(GOMLEntities.CLAIM_AUGMENT, pos, state);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, T baseBlockEntity) {
        if (world instanceof ServerWorld && baseBlockEntity instanceof ClaimAugmentBlockEntity entity) {
            // Parent is null and parent position is not null, assume we are just loading the augment from tags.
            if (entity.parentPosition != null) {
                var claims = ClaimUtils.getClaimsWithOrigin(world, entity.parentPosition);

                if (claims.isNotEmpty()) {
                    entity.claim = claims.collect(Collectors.toList()).get(0).getValue();
                    entity.markDirty();
                } else {
                    GetOffMyLawn.LOGGER.warn(String.format("An augment at %s tried to locate a parent at %s, but it could not be found!", entity.pos.toString(), entity.parentPosition.toString()));
                    world.breakBlock(pos, true);
                    return;
                }
            }

            if (entity.parentPosition == null) {
                GetOffMyLawn.LOGGER.warn(String.format("An augment at %s has an invalid parent and parent position! Removing now.", entity.pos.toString()));
                world.breakBlock(pos, true);
                return;
            }

            if (entity.claim.isDestroyed()) {
                world.breakBlock(pos, true);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        if (this.parentPosition != null) {
            tag.putLong(PARENT_POSITION_KEY, this.parentPosition.asLong());
        }

        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        this.parentPosition = BlockPos.fromLong(tag.getLong(PARENT_POSITION_KEY));

        if (this.augment == null) {
            if (getCachedState().getBlock() instanceof Augment) {
                initialize((Augment) getCachedState().getBlock());
            }
        }

        super.readNbt(tag);
    }

    public void remove() {
        if (this.claim != null) {
            claim.removeAugment(pos);
        }
    }

    @Deprecated
    @Nullable
    public ClaimAnchorBlockEntity getParent() {
        return this.claim != null ? this.claim.getBlockEntityInstance(this.world.getServer()) : null;
    }

    @Deprecated
    public void setParent(ClaimAnchorBlockEntity parent) {
        this.parentPosition = parent.getPos();
        parent.addChild(pos, this.getAugment());
    }

    public void setParent(BlockPos pos, Claim claim) {
        this.parentPosition = pos;
        claim.addAugment(pos, this.getAugment());
    }

    public void initialize(Augment augment) {
        this.augment = augment;
    }

    public Augment getAugment() {
        if (this.augment != null) {
            return augment;
        } else {
            return this.getCachedState().getBlock() instanceof Augment augment ? augment : Augment.noop();
        }
    }

    @Nullable
    public Claim getClaim() {
        return this.claim;
    }
}
