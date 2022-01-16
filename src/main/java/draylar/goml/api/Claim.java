package draylar.goml.api;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a claim on land with an origin {@link BlockPos}, owners, and other allowed players.
 * <p>While this class stores information about the origin of a claim, the actual bounding box is stored by the world.
 */
public class Claim {

    @Deprecated public static final String OWNER_KEY = "Owner"; // Legacy key for single-UUID owner

    public static final String POSITION_KEY = "Pos";
    public static final String OWNERS_KEY = "Owners";
    public static final String TRUSTED_KEY = "Trusted";
    public static final String ICON_KEY = "Icon";
    public static final String CUSTOM_DATA_KEY = "CustomData";

    private final Set<UUID> owners;
    private final Set<UUID> trusted = new HashSet<>();
    private final BlockPos origin;
    private Identifier world;
    @Nullable
    private ItemStack icon;

    private Map<DataKey<Object>, Object> customData = new HashMap<>();

    /**
     * Returns a {@link Claim} instance with the given owner and origin position.
     *
     * @param owners  list of {@link UUID}s of owners of the claim
     * @param origin  origin {@link BlockPos} of claim
     */
    public Claim(Set<UUID> owners, BlockPos origin) {
        this.owners = owners;
        this.origin = origin;
    }

    public Claim(Set<UUID> owners, Set<UUID> trusted, BlockPos origin) {
        this.owners = owners;
        this.trusted.addAll(trusted);
        this.origin = origin;
    }

    public boolean isOwner(PlayerEntity player) {
        return isOwner(player.getUuid());
    }

    protected boolean isOwner(UUID uuid) {
        return owners.contains(uuid);
    }

    public void addOwner(PlayerEntity player) {
        owners.add(player.getUuid());
    }

    public boolean hasPermission(PlayerEntity player) {
        return hasPermission(player.getUuid());
    }

    protected boolean hasPermission(UUID uuid) {
        return owners.contains(uuid) || trusted.contains(uuid);
    }

    public void trust(PlayerEntity player) {
        trusted.add(player.getUuid());
    }

    public void untrust(PlayerEntity player) {
        trusted.remove(player.getUuid());
    }

    /**
     * Returns the {@link UUID}s of the owners of the claim.
     *
     * <p>The owner is defined as the player who placed the claim block, or someone added through the goml command.
     *
     * @return  claim owner's UUIDs
     */
    public Set<UUID> getOwners() {
        return owners;
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    /**
     * Returns the origin position of the claim as a {@link BlockPos}.
     *
     * <p>The origin position of a claim is the position the center Claim Anchor was placed at.
     *
     * @return  origin position of this claim
     */
    public BlockPos getOrigin() {
        return origin;
    }

    /**
     * Serializes this {@link Claim} to a {@link NbtCompound} and returns it.
     *
     * <p>The following tags are stored at the top level of the tag:
     * <ul>
     * <li>"Owners" - list of {@link UUID}s of claim owners
     * <li>"Pos" - origin {@link BlockPos} of claim
     *
     * @return  this object serialized to a {@link NbtCompound}
     */
    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();

        // collect owner UUIDs into list
        NbtList ownersTag = new NbtList();
        owners.forEach(ownerUUID -> {
            ownersTag.add(NbtHelper.fromUuid(ownerUUID));
        });

        // collect trusted UUIDs into list
        NbtList trustedTag = new NbtList();
        trusted.forEach(trustedUUID -> {
            trustedTag.add(NbtHelper.fromUuid(trustedUUID));
        });

        nbt.put(OWNERS_KEY, ownersTag);
        nbt.put(TRUSTED_KEY, trustedTag);
        nbt.putLong(POSITION_KEY, origin.asLong());
        if (this.icon != null) {
            nbt.put(ICON_KEY, this.icon.writeNbt(new NbtCompound()));
        }

        var customData = new NbtCompound();

        for (var entry : this.customData.entrySet()) {
            var value = entry.getKey().serializer().apply(entry.getValue());

            if (value != null) {
                customData.put(entry.getKey().key(), value);
            }
        }

        nbt.put(CUSTOM_DATA_KEY, customData);

        return nbt;
    }

    /**
     * Uses the top level information in the given {@link NbtCompound} to construct a {@link Claim}.
     *
     * <p>This method expects to find the following tags at the top level of the tag:
     * <ul>
     * <li>"Owners" - {@link UUID}s of claim owners
     * <li>"Pos" - origin {@link BlockPos} of claim
     *
     * @param nbt  tag to deserialize information from
     * @return  {@link Claim} instance with information from tag
     */
    public static Claim fromNbt(NbtCompound nbt) {
        // Handle legacy data stored in "Owner" key, which is a single UUID
        if(nbt.containsUuid(OWNER_KEY)) {
            return new Claim(new HashSet<>(Collections.singleton(nbt.getUuid(OWNER_KEY))), BlockPos.fromLong(nbt.getLong(POSITION_KEY)));
        }

        // Collect UUID of owners
        Set<UUID> ownerUUIDs = new HashSet<>();
        NbtList ownersTag = nbt.getList(OWNERS_KEY, NbtType.INT_ARRAY);
        ownersTag.forEach(ownerUUID -> ownerUUIDs.add(NbtHelper.toUuid(ownerUUID)));

        // Collect UUID of trusted
        Set<UUID> trustedUUIDs = new HashSet<>();
        NbtList trustedTag = nbt.getList(TRUSTED_KEY, NbtType.INT_ARRAY);
        trustedTag.forEach(trustedUUID -> trustedUUIDs.add(NbtHelper.toUuid(trustedUUID)));

        var claim = new Claim(ownerUUIDs, trustedUUIDs, BlockPos.fromLong(nbt.getLong(POSITION_KEY)));

        if (nbt.contains(ICON_KEY, NbtElement.COMPOUND_TYPE)) {
            claim.icon = ItemStack.fromNbt(nbt.getCompound(ICON_KEY));
        }

        for (var key : nbt.getCompound(CUSTOM_DATA_KEY).getKeys()) {
            var dataKey = DataKey.getKey(key);

            if (dataKey != null) {
                claim.customData.put((DataKey<Object>) dataKey, dataKey.deserializer().apply(nbt.get(key)));
            }
        }

        return claim;
    }

    public Identifier getWorld() {
        return this.world != null ? this.world : new Identifier("undefined");
    }

    public ItemStack getIcon() {
        return this.icon != null ? this.icon.copy() : Items.STONE.getDefaultStack();
    }

    @Nullable
    public <T> T getData(DataKey<T> key) {
        try {
            return (T) this.customData.getOrDefault(key, key.defaultValue());
        } catch (Exception e) {
            return key.defaultValue();
        }
    }

    public <T> void setData(DataKey<T> key, T data) {
        if (data != null) {
            this.customData.put((DataKey<Object>) key, data);
        } else {
            this.customData.remove(key);
        }
    }

    public <T> void removeData(DataKey<T> key) {
        setData(key, null);
    }

    @ApiStatus.Internal
    public void internal_setIcon(ItemStack stack) {
        this.icon = stack.copy();
    }

    @ApiStatus.Internal
    public void internal_setWorld(Identifier world) {
        this.world = world;
    }
}
