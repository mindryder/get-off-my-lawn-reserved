package draylar.goml.block.entity;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.Augment;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import draylar.goml.registry.GOMLEntities;
import eu.pb4.polymer.api.utils.PolymerObject;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClaimAnchorBlockEntity extends BlockEntity implements PolymerObject {

    private static final String AUGMENT_LIST_KEY = "AugmentPositions";

    private final Map<BlockPos, Augment> augmentEntities = new HashMap<>();
    private final List<BlockPos> loadPositions = new ArrayList<>();
    private final List<PlayerEntity> previousTickPlayers = new ArrayList<>();
    private Claim claim;
    private ClaimBox box;

    public ClaimAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(GOMLEntities.CLAIM_ANCHOR, pos, state);
    }

    public static <T extends BlockEntity> void tick(World eWorld, BlockPos pos, BlockState state, T blockEntity) {
        if (eWorld instanceof ServerWorld world && blockEntity instanceof ClaimAnchorBlockEntity anchor) {

            // Claim is null, world probably just loaded, re-grab claim
            if (anchor.claim == null) {
                var collect = ClaimUtils.getClaimsAt(anchor.world, anchor.pos).collect(Collectors.toList());

                if (collect.isEmpty()) {
                    GetOffMyLawn.LOGGER.warn(String.format("A Claim Anchor at %s tried to initialize its claim, but one could not be found! Was the claim removed without the anchor?", anchor.pos));
                    world.breakBlock(pos, true);
                    for (var lPos : anchor.loadPositions) {
                        world.breakBlock(lPos, true);
                    }
                    return;
                } else {
                    var entry = collect.get(0);
                    anchor.claim = entry.getValue();
                    anchor.box = entry.getKey();
                }
            }

            // no augments, some queued from fromTag
            if (anchor.augmentEntities.isEmpty() && !anchor.loadPositions.isEmpty()) {
                for (BlockPos foundPos : anchor.loadPositions) {
                    BlockEntity foundEntity = anchor.world.getBlockEntity(foundPos);

                    if (foundEntity instanceof ClaimAugmentBlockEntity) {
                        anchor.augmentEntities.put(foundPos, ((ClaimAugmentBlockEntity) foundEntity).getAugment());
                    } else {
                        GetOffMyLawn.LOGGER.warn(String.format("A Claim Anchor at %s tried to load a child at %s, but none were found!", anchor.pos.toString(), foundPos.toString()));
                    }
                }

                anchor.loadPositions.clear();
            }

            int sizeX = anchor.box.getX();
            int sizeY = anchor.box.getY();
            int sizeZ = anchor.box.getZ();
            var playersInClaim = anchor.world.getEntitiesByClass(PlayerEntity.class, new Box(anchor.pos.add(-sizeX, -sizeY, -sizeZ), anchor.pos.add(sizeX, sizeY, sizeZ)), entity -> true);

            // Tick all augments
            for (var augment : anchor.augmentEntities.values()) {

                if (augment != null && augment.isEnabled(anchor.claim, world)) {
                    if (augment.ticks()) {
                        augment.tick(anchor.claim, anchor.world);
                        for (PlayerEntity playerEntity : playersInClaim) {
                            augment.playerTick(anchor.claim, playerEntity);
                        }
                    }

                    // Enter/Exit behavior
                    for (PlayerEntity playerEntity : playersInClaim) {
                        // this player was NOT in the claim last tick, call entry method
                        if (!anchor.previousTickPlayers.contains(playerEntity)) {
                            augment.onPlayerEnter(anchor.claim, playerEntity);
                        }
                    }

                    // Tick exit behavior
                    anchor.previousTickPlayers.stream().filter(player -> !playersInClaim.contains(player)).forEach(player -> {
                        augment.onPlayerExit(anchor.claim, player);
                    });
                }
            }

            // Reset players in claim
            anchor.previousTickPlayers.clear();
            anchor.previousTickPlayers.addAll(playersInClaim);

        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        NbtList positions = new NbtList();
        for (BlockPos loadPosition : this.loadPositions) {
            positions.add(NbtLong.of(loadPosition.asLong()));
        }
        for (Map.Entry<BlockPos, Augment> entry : this.augmentEntities.entrySet()) {
            positions.add(NbtLong.of(entry.getKey().asLong()));
        }

        tag.put(AUGMENT_LIST_KEY, positions);
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        NbtList positions = tag.getList(AUGMENT_LIST_KEY, NbtType.LONG);
        positions.forEach(sub -> {
            BlockPos foundPos = BlockPos.fromLong(((NbtLong) sub).longValue());
            this.loadPositions.add(foundPos);
        });

        super.readNbt(tag);
    }

    public void addChild(BlockPos pos, Augment augment) {
        this.augmentEntities.put(pos, augment);
        for (var player : this.previousTickPlayers) {
            augment.onPlayerEnter(this.claim, player);
        }
    }

    public void removeChild(BlockPos pos) {
        var augment = this.augmentEntities.remove(pos);
        if (augment != null) {
            for (var player : this.previousTickPlayers) {
                augment.onPlayerExit(this.claim, player);
            }
        }
    }

    @Nullable
    public Claim getClaim() {
        return this.claim;
    }

    @Nullable
    public ClaimBox getBox() {
        return this.box;
    }

    public void setClaim(Claim claim, ClaimBox box) {
        this.claim = claim;
        this.box = box;
    }

    public boolean hasAugment(Augment augment) {
        assert world != null;

        for (var entry : this.augmentEntities.entrySet()) {
            BlockPos position = entry.getKey();
            var block = world.getBlockState(position).getBlock();

            if (block instanceof Augment && block.equals(augment)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAugment() {
        return augmentEntities.size() > 0;
    }

    public Map<BlockPos, Augment> getAugments() {
        return augmentEntities;
    }

    public List<PlayerEntity> getPreviousTickPlayers() {
        return previousTickPlayers;
    }

    public void from(ClaimAnchorBlockEntity be) {
        this.previousTickPlayers.addAll(be.getPreviousTickPlayers());
        this.augmentEntities.putAll(be.getAugments());
    }

    @Override
    public void markRemoved() {

        // Make sure exit logic is run after claim is removed from world
        // This can happen while teleporting
        for (var augment : this.augmentEntities.values()) {
            if (augment != null && augment.isEnabled(this.claim, this.world)) {
                for (var player : this.previousTickPlayers) {
                    augment.onPlayerExit(this.claim, player);
                }
            }
        }

        // Reset players in claim
        this.previousTickPlayers.clear();

        super.markRemoved();
    }
}
