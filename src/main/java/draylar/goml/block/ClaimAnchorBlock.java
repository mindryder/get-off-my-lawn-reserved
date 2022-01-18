package draylar.goml.block;

import com.jamieswhiteshirt.rtree3i.Box;
import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.registry.GOMLEntities;
import draylar.goml.registry.GOMLTextures;
import draylar.goml.ui.ClaimPlayerListGui;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.function.IntSupplier;

@SuppressWarnings({"deprecation"})
public class ClaimAnchorBlock extends Block implements BlockEntityProvider, PolymerHeadBlock {

    private final IntSupplier radius;
    private final String texture;

    @Deprecated
    public ClaimAnchorBlock(Block.Settings settings, int radius) {
        this(settings, () -> radius, GOMLTextures.MISSING_TEXTURE);
    }

    public ClaimAnchorBlock(Block.Settings settings, IntSupplier radius, String texture) {
        super(settings);
        this.radius = radius;
        this.texture = texture;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world == null) {
            return;
        }

        if (!world.isClient()) {
            Claim claimInfo = new Claim(Collections.singleton(placer.getUuid()), Collections.emptySet(), pos, radius.getAsInt());
            claimInfo.internal_setIcon(new ItemStack(itemStack.getItem()));
            claimInfo.internal_setWorld(world.getRegistryKey().getValue());
            GetOffMyLawn.CLAIM.get(world).add(new ClaimBox(pos, radius.getAsInt()), claimInfo);

            // Assign claim to BE
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBlockEntity anchor) {
                anchor.setClaim(claimInfo);
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
            ClaimUtils.getClaimsAt(world, pos).forEach(claimedArea -> {
                if (ClaimUtils.canDestroyClaimBlock(claimedArea, null, pos)) {
                    GetOffMyLawn.CLAIM.get(world).remove(claimedArea.getKey());
                }
            });
        }


        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world == null || world.isClient()) {
            return;
        }

        ClaimUtils.getClaimsAt(world, pos).forEach(claimedArea -> {
            if (ClaimUtils.canDestroyClaimBlock(claimedArea, player, pos)) {
                GetOffMyLawn.CLAIM.get(world).remove(claimedArea.getKey());
            }
        });

        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit) {
        if (playerEntity instanceof ServerPlayerEntity player && !player.isSneaking()) {
            var blockEntity = world.getBlockEntity(pos, GOMLEntities.CLAIM_ANCHOR);
            if (blockEntity.isPresent()) {
                blockEntity.get().getClaim().openUi(player);
            }

            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, playerEntity, hand, hit);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldView, BlockPos pos) {
        if (worldView == null) {
            return true;
        }

        var radius = this.radius.getAsInt();

        if (worldView instanceof World world) {
            Box checkBox = Box.create(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);

            if (GetOffMyLawn.CONFIG.isBlacklisted(world, checkBox)) {
                return false;
            }
        }

        return ClaimUtils.getClaimsInBox(worldView, pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius)).isEmpty();
    }

    public int getRadius() {
        return this.radius.getAsInt();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ClaimAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ClaimAnchorBlockEntity::tick;
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
