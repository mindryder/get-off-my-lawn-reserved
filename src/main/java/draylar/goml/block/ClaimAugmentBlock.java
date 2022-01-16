package draylar.goml.block;

import draylar.goml.api.Augment;
import draylar.goml.api.Claim;
import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.block.entity.ClaimAugmentBlockEntity;
import draylar.goml.registry.GOMLTextures;
import eu.pb4.polymer.api.block.PolymerHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ClaimAugmentBlock extends Block implements Augment, BlockEntityProvider, PolymerHeadBlock {

    private final String texture;
    private BooleanSupplier isEnabled = () -> true;

    @Deprecated
    public ClaimAugmentBlock(Settings settings) {
        this(settings, GOMLTextures.MISSING_TEXTURE);
    }

    public ClaimAugmentBlock(Settings settings, String texture) {
        super(settings);
        this.texture = texture;
    }

    @ApiStatus.Internal
    public void setEnabledCheck(BooleanSupplier check) {
        this.isEnabled = check;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (world instanceof World) {
            for (Direction direction : Direction.values()) {
                var blockEntity = world.getBlockEntity(pos.offset(direction));

                if (blockEntity instanceof ClaimAnchorBlockEntity claimAnchorBlockEntity && claimAnchorBlockEntity.getClaim() != null) {
                    return this.canPlace(claimAnchorBlockEntity.getClaim(), (World) world, pos, claimAnchorBlockEntity);
                } else if (blockEntity instanceof ClaimAugmentBlockEntity claimAugmentBlockEntity && claimAugmentBlockEntity.getParent() != null && claimAugmentBlockEntity.getParent().getClaim() != null) {
                    return this.canPlace(claimAugmentBlockEntity.getParent().getClaim(), (World) world, pos, claimAugmentBlockEntity.getParent());
                }
            }
        }

        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world == null || world.isClient()) {
            return;
        }

        ClaimAugmentBlockEntity thisBE = (ClaimAugmentBlockEntity) world.getBlockEntity(pos);

        if (thisBE == null) {
            return;
        }

        thisBE.initialize(this);

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            BlockState offsetState = world.getBlockState(offsetPos);
            Block offsetBlock = offsetState.getBlock();

            // Neighbor is a core element, set parent directly
            if (offsetBlock instanceof ClaimAnchorBlock) {
                ClaimAnchorBlockEntity coreBE = (ClaimAnchorBlockEntity) world.getBlockEntity(offsetPos);
                thisBE.setParent(coreBE);
                return;
            }

            // Neighbor is another augment, grab parent from it
            if (offsetBlock instanceof ClaimAugmentBlock) {
                ClaimAugmentBlockEntity otherAugmentBE = (ClaimAugmentBlockEntity) world.getBlockEntity(offsetPos);

                if (otherAugmentBE != null) {
                    thisBE.setParent(otherAugmentBE.getParent());
                }

                return;
            }
        }


        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world == null || world.isClient()) {
            return;
        }

        if (newState.getBlock() != state.getBlock()) {
            ClaimAugmentBlockEntity be = (ClaimAugmentBlockEntity) world.getBlockEntity(pos);
            be.remove();
        }


        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            ClaimAugmentBlockEntity be = (ClaimAugmentBlockEntity) world.getBlockEntity(pos);
            be.remove();
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean canPlace(Claim claim, World world, BlockPos pos, ClaimAnchorBlockEntity anchor) {
        return !anchor.hasAugment(this);
    }

    @Override
    public boolean isEnabled(Claim claim, World world) {
        return this.isEnabled.getAsBoolean();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ClaimAugmentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ClaimAugmentBlockEntity::tick;
    }

    @Override
    public String getPolymerSkinValue(BlockState state) {
        return this.texture;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.PLAYER_HEAD;
    }
}
