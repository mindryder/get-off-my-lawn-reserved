package draylar.goml.api;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public record ClaimBox(com.jamieswhiteshirt.rtree3i.Box rtree3iBox, Box minecraftBox, BlockPos origin, int radius, int radiusY, boolean noShift) {
    public static final ClaimBox EMPTY = new ClaimBox(BlockPos.ORIGIN, 0, 0, true);

    public ClaimBox(BlockPos origin, int radius, int radiusY) {
        this(
                origin,
                radius,
                radiusY,
                false
        );
    }

    public ClaimBox(BlockPos origin, int radius, int radiusY, boolean noShift) {
        this(
                noShift ? createBoxNoShift(origin, radius, radiusY) : createBox(origin, radius, radiusY),
                new Box(origin.add(-radius, -radiusY, -radius), noShift ? origin.add(radius, radiusY, radius) : origin.add(radius + 1, radiusY + 1, radius + 1)),
                origin, radius, radiusY, noShift
        );
    }

    private static com.jamieswhiteshirt.rtree3i.Box createBox(BlockPos origin, int radius, int radiusY) {
        BlockPos lower = origin.add(-radius, -radiusY, -radius);
        BlockPos upper = origin.add(radius + 1, radiusY + 1, radius + 1);
        return com.jamieswhiteshirt.rtree3i.Box.create(lower.getX(), lower.getY(), lower.getZ(), upper.getX(), upper.getY(), upper.getZ());
    }

    private static com.jamieswhiteshirt.rtree3i.Box createBoxNoShift(BlockPos origin, int radius, int radiusY) {
        BlockPos lower = origin.add(-radius, -radiusY, -radius);
        BlockPos upper = origin.add(radius, radiusY, radius);
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

    public static ClaimBox readNbt(NbtCompound tag, int i) {
        BlockPos originPos = BlockPos.fromLong(tag.getLong("OriginPos"));
        var radius = tag.getInt("Radius");
        var height = tag.contains("Height") ? tag.getInt("Height") : radius;
        if (radius > 0 && height > 0) {
            return new ClaimBox(originPos, radius, height, tag.getBoolean("NoShift"));
        }
        return EMPTY;
    }

    public NbtElement toNbt() {
        NbtCompound boxTag = new NbtCompound();

        boxTag.putLong("OriginPos", this.getOrigin().asLong());
        boxTag.putInt("Radius", this.getRadius());
        boxTag.putInt("Height", this.getY());
        boxTag.putBoolean("NoShift", this.noShift());

        return boxTag;
    }
}
