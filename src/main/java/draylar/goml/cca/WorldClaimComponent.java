package draylar.goml.cca;

import com.jamieswhiteshirt.rtree3i.ConfigurationBuilder;
import com.jamieswhiteshirt.rtree3i.RTreeMap;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WorldClaimComponent implements ClaimComponent {

    private RTreeMap<ClaimBox, Claim> claims = RTreeMap.create(new ConfigurationBuilder().star().build(), ClaimBox::toBox);
    private final World world;

    public WorldClaimComponent(World world) {
        this.world = world;
    }

    @Override
    public RTreeMap<ClaimBox, Claim> getClaims() {
        return claims;
    }

    @Override
    public void add(ClaimBox box, Claim info) {
        this.claims = this.claims.put(box, info);
    }

    @Override
    public void remove(ClaimBox box) {
        this.claims = this.claims.remove(box);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.claims = RTreeMap.create(new ConfigurationBuilder().star().build(), ClaimBox::rtree3iBox);
        var world = this.world.getRegistryKey().getValue();
        NbtList NbtList = tag.getList("Claims", NbtType.COMPOUND);

        NbtList.forEach(child -> {
            NbtCompound childCompound = (NbtCompound) child;
            ClaimBox box = boxFromTag((NbtCompound) childCompound.get("Box"));
            if (box != null) {
                Claim claimInfo = Claim.fromNbt((NbtCompound) childCompound.get("Info"));
                claimInfo.internal_setWorld(world);
                claimInfo.internal_setClaimBox(box);
                add(box, claimInfo);
            }
        });
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList NbtListClaims = new NbtList();

        claims.entries().forEach(claim -> {
            NbtCompound claimTag = new NbtCompound();

            claimTag.put("Box", serializeBox(claim.getKey()));
            claimTag.put("Info", claim.getValue().asNbt());

            NbtListClaims.add(claimTag);
        });

        tag.put("Claims", NbtListClaims);
    }

    public NbtCompound serializeBox(ClaimBox box) {
        NbtCompound boxTag = new NbtCompound();

        boxTag.putLong("OriginPos", box.getOrigin().asLong());
        boxTag.putInt("Radius", box.getRadius());
        boxTag.putInt("Height", box.getY());

        return boxTag;
    }

    @Nullable
    public ClaimBox boxFromTag(NbtCompound tag) {
        BlockPos originPos = BlockPos.fromLong(tag.getLong("OriginPos"));
        var radius = tag.getInt("Radius");
        var height = tag.contains("Height") ? tag.getInt("Height") : radius;
        if (radius > 0 && height > 0) {
            return new ClaimBox(originPos, radius, height);
        }
        return null;
    }
}
