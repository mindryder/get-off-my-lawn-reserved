package draylar.goml.api;

import com.mojang.authlib.GameProfile;
import draylar.goml.GetOffMyLawn;
import draylar.goml.registry.GOMLBlocks;
import eu.pb4.common.protection.api.ProtectionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public final class GomlProtectionProvider implements ProtectionProvider {
    public static ProtectionProvider INSTANCE = new GomlProtectionProvider();
    private GomlProtectionProvider() {}

    @Override
    public boolean isProtected(World world, BlockPos pos) {
        if (world.getServer() == null) {
            return false;
        }
        return ClaimUtils.getClaimsAt(world, pos).isNotEmpty();
    }

    @Override
    public boolean isAreaProtected(World world, Box box) {
        if (world.getServer() == null) {
            return false;
        }
        return ClaimUtils.getClaimsInBox(world, BlockPos.ofFloored(box.minX, box.minY, box.minZ), BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ)).isNotEmpty();
    }

    @Override
    public boolean canBreakBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        if (world.getServer() == null) {
            return true;
        }

        if (player != null) {
            return ClaimUtils.canModify(world, pos, player);
        } else {
            var claims = ClaimUtils.getClaimsAt(world, pos);
            return claims.isEmpty() || claims.anyMatch((c) -> c.getValue().hasPermission(profile.getId()));
        }
    }

    @Override
    public boolean canExplodeBlock(World world, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable PlayerEntity player) {
        if (world.getServer() == null) {
            return true;
        }
        return ClaimUtils.canExplosionDestroy(world, pos, player);
    }

    @Override
    public boolean canPlaceBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        return this.canBreakBlock(world, pos, profile, player);
    }

    @Override
    public boolean canInteractBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        if (world.getServer() == null) {
            return true;
        }
        return GetOffMyLawn.CONFIG.canInteract(world.getBlockState(pos).getBlock()) || this.canBreakBlock(world, pos, profile, player);
    }

    @Override
    public boolean canInteractEntity(World world, Entity entity, GameProfile profile, @Nullable PlayerEntity player) {
        if (world.getServer() == null) {
            return true;
        }
        return GetOffMyLawn.CONFIG.canInteract(entity) || this.canBreakBlock(world, entity.getBlockPos(), profile, player);
    }

    @Override
    public boolean canDamageEntity(World world, Entity entity, GameProfile profile, @Nullable PlayerEntity player) {
        if (world.getServer() == null) {
            return true;
        }

        if (entity instanceof PlayerEntity attackedPlayer) {
            var claims = ClaimUtils.getClaimsAt(world, entity.getBlockPos());

            if (claims.isEmpty()) {
                return true;
            } else {
                claims = claims.filter((e) -> e.getValue().hasAugment(GOMLBlocks.PVP_ARENA.getFirst()));

                if (claims.isEmpty()) {
                    return GetOffMyLawn.CONFIG.enablePvPinClaims;
                } else {
                    var obj = new MutableBoolean();

                    claims.forEach((e) -> {
                        var claim = e.getValue();
                        if (!obj.getValue()) {
                            return;
                        }

                        obj.setValue(switch (claim.getData(GOMLBlocks.PVP_ARENA.getFirst().key)) {
                            case EVERYONE -> true;
                            case DISABLED -> player != null && ClaimUtils.isInAdminMode(player);
                            case TRUSTED -> claim.hasPermission(profile.getId()) && claim.hasPermission(attackedPlayer);
                            case UNTRUSTED -> !claim.hasPermission(profile.getId()) && !claim.hasPermission(attackedPlayer);
                        });
                    });

                    return obj.getValue();
                }
            }
        }

        return this.canBreakBlock(world, entity.getBlockPos(), profile, player)
                || (GetOffMyLawn.CONFIG.allowDamagingUnnamedHostileMobs && entity instanceof HostileEntity && entity.getCustomName() == null);
    }
}
