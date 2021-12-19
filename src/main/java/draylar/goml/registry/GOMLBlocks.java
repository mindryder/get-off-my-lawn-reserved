package draylar.goml.registry;

import com.mojang.datafixers.util.Pair;
import draylar.goml.GetOffMyLawn;
import draylar.goml.block.ClaimAnchorBlock;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.block.augment.*;
import draylar.goml.item.TooltippedBlockItem;
import eu.pb4.polymer.api.item.PolymerHeadBlockItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class GOMLBlocks {

    public static final List<ClaimAnchorBlock> ANCHORS = new ArrayList<>();
    public static final List<ClaimAugmentBlock> AUGMENTS = new ArrayList<>();

    public static final Pair<ClaimAnchorBlock, Item> MAKESHIFT_CLAIM_ANCHOR = register("makeshift_claim_anchor", () -> GetOffMyLawn.CONFIG.makeshiftRadius, 10, GOMLTextures.MAKESHIFT_CLAIM_ANCHOR);
    public static final Pair<ClaimAnchorBlock, Item> REINFORCED_CLAIM_ANCHOR = register("reinforced_claim_anchor", () -> GetOffMyLawn.CONFIG.reinforcedRadius, 10, GOMLTextures.REINFORCED_CLAIM_ANCHOR);
    public static final Pair<ClaimAnchorBlock, Item> GLISTENING_CLAIM_ANCHOR = register("glistening_claim_anchor", () -> GetOffMyLawn.CONFIG.glisteningRadius, 15, GOMLTextures.GLISTENING_CLAIM_ANCHOR);
    public static final Pair<ClaimAnchorBlock, Item> CRYSTAL_CLAIM_ANCHOR = register("crystal_claim_anchor", () -> GetOffMyLawn.CONFIG.crystalRadius, 20, GOMLTextures.CRYSTAL_CLAIM_ANCHOR);
    public static final Pair<ClaimAnchorBlock, Item> EMERADIC_CLAIM_ANCHOR = register("emeradic_claim_anchor", () -> GetOffMyLawn.CONFIG.emeradicRadius, 20, GOMLTextures.EMERADIC_CLAIM_ANCHOR);
    public static final Pair<ClaimAnchorBlock, Item> WITHERED_CLAIM_ANCHOR = register("withered_claim_anchor", () -> GetOffMyLawn.CONFIG.witheredRadius, 25, GOMLTextures.WITHERED_CLAIM_ANCHOR);

    public static final Pair<ClaimAugmentBlock, Item> ENDER_BINDING = register("ender_binding", new EnderBindingAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.ENDER_BINDING), 2);
    public static final Pair<ClaimAugmentBlock, Item> LAKE_SPIRIT_GRACE = register("lake_spirit_grace", new LakeSpiritGraceAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.LAKE_SPIRIT_GRACE), 2);
    public static final Pair<ClaimAugmentBlock, Item> ANGELIC_AURA = register("angelic_aura", new AngelicAuraAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.ANGELIC_AURA), 2);
    public static final Pair<ClaimAugmentBlock, Item> HEAVEN_WINGS = register("heaven_wings", new HeavenWingsAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.HEAVEN_WINGS), 2);
    public static final Pair<ClaimAugmentBlock, Item> VILLAGE_CORE = register("village_core", new ClaimAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.VILLAGE_CORE), 2);
    public static final Pair<ClaimAugmentBlock, Item> WITHERING_SEAL = register("withering_seal", new WitheringSealAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.WITHERING_SEAL), 2);
    public static final Pair<ClaimAugmentBlock, Item> CHAOS_ZONE = register("chaos_zone", new ChaosZoneAugmentBlock(FabricBlockSettings.of(Material.STONE).hardness(10).resistance(3600000.0F), GOMLTextures.CHAOS_ZONE), 2);

    private static Pair<ClaimAnchorBlock, Item> register(String name, IntSupplier radius, float hardness, String texture) {
        var claimAnchorBlock = Registry.register(
                    Registry.BLOCK,
                    GetOffMyLawn.id(name),
                    new ClaimAnchorBlock(FabricBlockSettings.of(Material.STONE).strength(hardness, 3600000.0F), radius, texture)
            );


        var registeredItem = Registry.register(Registry.ITEM, GetOffMyLawn.id(name), new PolymerHeadBlockItem(claimAnchorBlock, new Item.Settings().group(GetOffMyLawn.GROUP)));
        ANCHORS.add(claimAnchorBlock);
        return Pair.of(claimAnchorBlock, registeredItem);
    }

    private static Pair<ClaimAugmentBlock, Item> register(String name, ClaimAugmentBlock augment) {
        ClaimAugmentBlock registered = Registry.register(
                Registry.BLOCK,
                GetOffMyLawn.id(name),
                augment
        );

        Item registeredItem = Registry.register(Registry.ITEM, GetOffMyLawn.id(name), new PolymerHeadBlockItem(augment, new Item.Settings().group(GetOffMyLawn.GROUP)));
        AUGMENTS.add(registered);
        return Pair.of(augment, registeredItem);
    }

    private static Pair<ClaimAugmentBlock, Item> register(String name, ClaimAugmentBlock augment, int tooltipLines) {
        ClaimAugmentBlock registered = Registry.register(
                Registry.BLOCK,
                GetOffMyLawn.id(name),
                augment
        );

        Item registeredItem = Registry.register(Registry.ITEM, GetOffMyLawn.id(name), new TooltippedBlockItem(augment, new Item.Settings().group(GetOffMyLawn.GROUP), tooltipLines));
        AUGMENTS.add(registered);
        return Pair.of(augment, registeredItem);
    }

    public static void init() {
        // NO-OP
    }

    private GOMLBlocks() {
        // NO-OP
    }
}
