package draylar.goml.api;

import com.jamieswhiteshirt.rtree3i.Box;
import net.minecraft.util.math.BlockPos;

public record ClaimBox(BlockPos origin, int radius, int radiusY) {
    public Box toBox() {
        BlockPos lower = origin.add(-radius, -radiusY, -radius);
        BlockPos upper = origin.add(radius, radiusY, radius);
        return Box.create(lower.getX(), lower.getY(), lower.getZ(), upper.getX(), upper.getY(), upper.getZ());
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
