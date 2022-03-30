package draylar.goml.block.augment;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.DataKey;
import draylar.goml.block.ClaimAugmentBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;
import java.util.UUID;

public class ForceFieldAugmentBlock extends ClaimAugmentBlock {

    public static final DataKey<Set<UUID>> UUID_KEY = DataKey.ofUuidSet(GetOffMyLawn.id("force_field/uuids"));
    public static final DataKey<Boolean> WHITELIST_KEY = DataKey.ofBoolean(GetOffMyLawn.id("force_field/whitelist"), true);

    public ForceFieldAugmentBlock(Settings settings, String texture) {
        super(settings, texture);
    }

    @Override
    public void onPlayerEnter(Claim claim, PlayerEntity player) {
        if (shouldBlock(claim, player)) {
            var claimPos = claim.getOrigin();

            var dir = player.getPos().subtract(Vec3d.ofCenter(claimPos));

            var l = dir.length();

            var move = dir.multiply(0.5 / l);

            //player.setVelocity(move.x, move.y, move.z);
            player.velocityModified = true;
        }
    }

    @Override
    public void playerTick(Claim claim, PlayerEntity player) {
        if (shouldBlock(claim, player)) {
            var claimPos = claim.getOrigin();

            var dir = player.getPos().subtract(Vec3d.ofCenter(claimPos));

            var l = claim.getRadius() / dir.length();

            var move = dir.multiply(0.8 * l, 0.3 * l, 0.8 * l);

            player.setVelocity(move.x, move.y, move.z);
            player.velocityModified = true;
        }
    }

    @Override
    public boolean ticks() {
        return true;
    }

    public boolean shouldBlock(Claim claim, PlayerEntity player) {
        var uuids = claim.getData(UUID_KEY);

        if (claim.hasPermission(player) || ClaimUtils.isInAdminMode(player)) {
            return false;
        }
        var doThing = uuids.contains(player.getUuid());

        if (claim.getData(WHITELIST_KEY)) {
            doThing = !doThing;
        }

        return doThing;
    }
}
