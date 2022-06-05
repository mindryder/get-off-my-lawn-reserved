package draylar.goml.item;

import com.jamieswhiteshirt.rtree3i.Box;
import draylar.goml.GetOffMyLawn;
import draylar.goml.api.ClaimUtils;
import draylar.goml.block.ClaimAnchorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClaimAnchorBlockItem extends TooltippedBlockItem {

    private final ClaimAnchorBlock claimBlock;

    public ClaimAnchorBlockItem(ClaimAnchorBlock block, Settings settings, int lines) {
        super(block, settings, lines);
        this.claimBlock = block;
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        var pos = context.getBlockPos();
        var radius = this.claimBlock.getRadius();
        if (radius <= 0 && !ClaimUtils.isInAdminMode(context.getPlayer())) {
            context.getPlayer().sendMessage(GetOffMyLawn.CONFIG.prefix(Text.translatable("text.goml.cant_place_claim.admin_only").formatted(Formatting.RED)), false);
            return false;
        }

        radius = Math.max(radius, 1);
        var vertRadius = GetOffMyLawn.CONFIG.claimProtectsFullWorldHeight ? Short.MAX_VALUE : radius;
        var checkBox = Box.create(pos.getX() - radius, pos.getY() - vertRadius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + vertRadius, pos.getZ() + radius);

        if (!ClaimUtils.isInAdminMode(context.getPlayer())) {
            var count = ClaimUtils.getClaimsOwnedBy(context.getWorld(), context.getPlayer().getUuid()).count();
            if (GetOffMyLawn.CONFIG.maxClaimsPerPlayer != -1
                    && count >= GetOffMyLawn.CONFIG.maxClaimsPerPlayer
            ) {
                context.getPlayer().sendMessage(GetOffMyLawn.CONFIG.prefix(Text.translatable("text.goml.cant_place_claim.max_count_reached", count, GetOffMyLawn.CONFIG.maxClaimsPerPlayer).formatted(Formatting.RED)), false);
                return false;
            }

            if (GetOffMyLawn.CONFIG.isBlacklisted(context.getWorld(), checkBox)) {
                context.getPlayer().sendMessage(GetOffMyLawn.CONFIG.prefix(Text.translatable("text.goml.cant_place_claim.blacklisted_area", context.getWorld().getRegistryKey().getValue().toString(), context.getBlockPos().toShortString()).formatted(Formatting.RED)), false);
                return false;
            }
        }


        var claims = ClaimUtils.getClaimsInBox(context.getWorld(), pos.add(-radius, -vertRadius, -radius), pos.add(radius, vertRadius, radius));
        if (claims.isNotEmpty()) {
            var list = Text.literal("");

            claims.forEach((c) -> {
                var box = c.getKey().toBox();

                list.append(Text.literal("[").formatted(Formatting.GRAY)
                        .append(Text.literal(box.x1() + ", " + box.y1() + ", " + box.z1()).formatted(Formatting.WHITE))
                        .append(" | ")
                        .append(Text.literal(box.x2() + ", " + box.y2() + ", " + box.z2()).formatted(Formatting.WHITE))
                        .append("] ")
                );
            });

            context.getPlayer().sendMessage(GetOffMyLawn.CONFIG.prefix(Text.translatable("text.goml.cant_place_claim.collides_with", list).formatted(Formatting.RED)), false);
            return false;
        }


        return super.canPlace(context, state);


    }
}
