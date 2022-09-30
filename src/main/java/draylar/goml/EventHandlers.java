package draylar.goml;

import com.jamieswhiteshirt.rtree3i.Entry;
import com.jamieswhiteshirt.rtree3i.Selection;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.PermissionReason;
import draylar.goml.api.event.ClaimEvents;
import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.registry.GOMLBlocks;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EventHandlers {

    private EventHandlers() {
        // NO-OP
    }

    public static void init() {
        registerBreakBlockCallback();
        registerInteractBlockCallback();
        registerAttackEntityCallback();
        registerInteractEntityCallback();
        registerAnchorAttackCallback();
    }

    private static void registerInteractEntityCallback() {
        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if (entity instanceof PlayerEntity) {
                return GetOffMyLawn.CONFIG.enablePvPinClaims || ClaimUtils.isInAdminMode(playerEntity) ? ActionResult.PASS : ActionResult.FAIL;
            }

            if (GetOffMyLawn.CONFIG.allowedEntityInteraction.contains(Registry.ENTITY_TYPE.getId(entity.getType()))) {
                return ActionResult.PASS;
            }

            if (entity instanceof Tameable tameable && tameable.getOwner() == playerEntity) {
                return ActionResult.PASS;
            }

            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, entity.getBlockPos());
            return testPermission(claimsFound, playerEntity, hand, entity.getBlockPos(), PermissionReason.ENTITY_PROTECTED);
        });
    }

    private static void registerAttackEntityCallback() {
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if (ClaimUtils.isInAdminMode(playerEntity)) {
                return ActionResult.PASS;
            }

            if (((GetOffMyLawn.CONFIG.allowDamagingUnnamedHostileMobs && entity.getCustomName() == null)
                    || (GetOffMyLawn.CONFIG.allowDamagingNamedHostileMobs && entity.getCustomName() != null))
                    && entity instanceof HostileEntity
            ) {
                return ActionResult.PASS;
            }

            if (entity instanceof PlayerEntity attackedPlayer) {
                var claims = ClaimUtils.getClaimsAt(world, entity.getBlockPos());

                if (claims.isEmpty()) {
                    return ActionResult.PASS;
                } else {
                    claims = claims.filter((e) -> e.getValue().getBlockEntityInstance(playerEntity.getServer()).hasAugment(GOMLBlocks.PVP_ARENA.getFirst()));

                    if (claims.isEmpty()) {
                        return GetOffMyLawn.CONFIG.enablePvPinClaims ? ActionResult.PASS : ActionResult.FAIL;
                    } else {
                        var obj = new MutableObject<>(ActionResult.PASS);

                        claims.forEach((e) -> {
                            var claim = e.getValue();
                            if (obj.getValue() == ActionResult.FAIL) {
                                return;
                            }

                            obj.setValue(switch (claim.getData(GOMLBlocks.PVP_ARENA.getFirst().key)) {
                                case EVERYONE -> ActionResult.PASS;
                                case DISABLED -> ClaimUtils.isInAdminMode(playerEntity) ? ActionResult.PASS : ActionResult.FAIL;
                                case TRUSTED -> claim.hasPermission(playerEntity) && claim.hasPermission(attackedPlayer)
                                        ? ActionResult.PASS : ActionResult.FAIL;
                                case UNTRUSTED -> !claim.hasPermission(playerEntity) && !claim.hasPermission(attackedPlayer)
                                        ? ActionResult.PASS : ActionResult.FAIL;
                            });
                        });

                        return obj.getValue();
                    }
                }
            }

            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, entity.getBlockPos());
            return testPermission(claimsFound, playerEntity, hand, entity.getBlockPos(), PermissionReason.ENTITY_PROTECTED);
        });
    }

    private static void registerInteractBlockCallback() {
        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (!(playerEntity.getStackInHand(hand).getItem() instanceof BlockItem)) {
                var blockState = world.getBlockState(blockHitResult.getBlockPos());

                if (GetOffMyLawn.CONFIG.allowedBlockInteraction.contains(Registry.BLOCK.getId(blockState.getBlock()))) {
                    return ActionResult.PASS;
                }
            }

            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, blockHitResult.getBlockPos());
            return testPermission(claimsFound, playerEntity, hand, blockHitResult.getBlockPos(), PermissionReason.AREA_PROTECTED);
        });

        // handle placing blocks at side of block not in claim
        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (!(playerEntity.getStackInHand(hand).getItem() instanceof BlockItem)) {
                var blockState = world.getBlockState(blockHitResult.getBlockPos());

                if (GetOffMyLawn.CONFIG.allowedBlockInteraction.contains(Registry.BLOCK.getId(blockState.getBlock()))) {
                    return ActionResult.PASS;
                }
            }

            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, blockHitResult.getBlockPos().offset(blockHitResult.getSide()));
            return testPermission(claimsFound, playerEntity, hand, blockHitResult.getBlockPos(), PermissionReason.AREA_PROTECTED);
        });
    }

    private static void registerBreakBlockCallback() {
        AttackBlockCallback.EVENT.register((playerEntity, world, hand, blockPos, direction) -> {
            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, blockPos);
            return testPermission(claimsFound, playerEntity, hand, blockPos, PermissionReason.BLOCK_PROTECTED);
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            Selection<Entry<ClaimBox, Claim>> claimsFound = ClaimUtils.getClaimsAt(world, pos);
            ActionResult result = testPermission(claimsFound, player, Hand.MAIN_HAND, pos, PermissionReason.BLOCK_PROTECTED);
            return !result.equals(ActionResult.FAIL);
        });
    }

    private static void registerAnchorAttackCallback() {
        AttackBlockCallback.EVENT.register((playerEntity, world, hand, blockPos, direction) -> {
            var be = world.getBlockEntity(blockPos);

            if (be instanceof ClaimAnchorBlockEntity) {
                if (!(((ClaimAnchorBlockEntity) be).getClaim().isOwner(playerEntity) || ClaimUtils.isInAdminMode(playerEntity))) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }

    @ApiStatus.Internal
    public static ActionResult testPermission(Selection<Entry<ClaimBox, Claim>> claims, PlayerEntity player, Hand hand, BlockPos pos, PermissionReason reason) {
        if (!claims.isEmpty()) {
            boolean noPermission = claims.anyMatch((Entry<ClaimBox, Claim> boxInfo) -> !boxInfo.getValue().hasPermission(player));

            if (noPermission && !ClaimUtils.isInAdminMode(player)) {
                ActionResult check = ClaimEvents.PERMISSION_DENIED.invoker().check(player, player.world, hand, pos, reason);

                if (check.isAccepted() || check.equals(ActionResult.PASS)) {
                    player.sendMessage(reason.getReason(), true);
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }
}
