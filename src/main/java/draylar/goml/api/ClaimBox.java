package draylar.goml.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record ClaimBox(com.jamieswhiteshirt.rtree3i.Box rtree3iBox, Box minecraftBox, BlockPos origin, int radius, int radiusY) {
    public static final ClaimBox EMPTY = new ClaimBox(BlockPos.ORIGIN, 0, 0);

    public ClaimBox(BlockPos origin, int radius, int radiusY) {
        this(
                createBox(origin, radius, radiusY),
                Box.of(Vec3d.ofCenter(origin), radius * 2, radiusY * 2, radius  * 2),
                origin, radius, radiusY
        );
    }

    private static com.jamieswhiteshirt.rtree3i.Box createBox(BlockPos origin, int radius, int radiusY) {
        BlockPos lower = origin.add(-radius, -radiusY, -radius);
        BlockPos upper = origin.add(radius + 1, radiusY + 1, radius + 1);
        return com.jamieswhiteshirt.rtree3i.Box.create(lower.getX(), lower.getY(), lower.getZ(), upper.getX(), upper.getY(), upper.getZ());
    }

    public com.jamieswhiteshirt.rtree3i.Box toBox() {
        return this.rtree3iBox;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public int getRadius() {
        return this.radius;
    }

    public int getX() {
        return this.radius;
    }

    public int getY() {
        return this.radiusY;
    }

    public int getZ() {
        return this.radius;
    }
}
