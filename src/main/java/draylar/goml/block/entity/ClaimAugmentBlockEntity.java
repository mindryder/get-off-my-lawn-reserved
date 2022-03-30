package draylar.goml.block.entity;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Augment;
import draylar.goml.registry.GOMLEntities;
import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClaimAugmentBlockEntity extends BlockEntity implements PolymerObject {

    private static final String PARENT_POSITION_KEY = "ParentPosition";
    @Nullable
    private ClaimAnchorBlockEntity parent;
    private BlockPos parentPosition;
    private Augment augment;

    public ClaimAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(GOMLEntities.CLAIM_AUGMENT, pos, state);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, T baseBlockEntity) {
        if (world instanceof ServerWorld && baseBlockEntity instanceof ClaimAugmentBlockEntity entity) {
            // Parent is null and parent position is not null, assume we are just loading the augment from tags.
            if (entity.parent == null && entity.parentPosition != null) {
                BlockEntity blockEntity = entity.world.getBlockEntity(entity.parentPosition);

                if (blockEntity instanceof ClaimAnchorBlockEntity) {
                    entity.parent = (ClaimAnchorBlockEntity) blockEntity;
                    entity.markDirty();
                } else {
                    GetOffMyLawn.LOGGER.warn(String.format("An augment at %s tried to locate a parent at %s, but it could not be found!", entity.pos.toString(), entity.parentPosition.toString()));
                    world.breakBlock(pos, true);
                }
            }

            if (entity.parent == null && entity.parentPosition == null) {
                GetOffMyLawn.LOGGER.warn(String.format("An augment at %s has an invalid parent and parent position! Removing now.", entity.pos.toString()));
                world.breakBlock(pos, true);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        if (parent != null) {
            tag.putLong(PARENT_POSITION_KEY, parent.getPos().asLong());
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
        if (this.parent != null) {
            parent.removeChild(pos);
        }
    }

    @Nullable
    public ClaimAnchorBlockEntity getParent() {
        return parent;
    }

    public void setParent(ClaimAnchorBlockEntity parent) {
        this.parent = parent;
        parent.addChild(pos, this.getAugment());
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
}
