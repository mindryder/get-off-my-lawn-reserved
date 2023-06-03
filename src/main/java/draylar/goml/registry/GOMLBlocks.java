package draylar.goml.registry;

import com.mojang.datafixers.util.Pair;
import draylar.goml.GetOffMyLawn;
import draylar.goml.block.ClaimAnchorBlock;
import draylar.goml.block.ClaimAugmentBlock;
import draylar.goml.block.SelectiveClaimAugmentBlock;
import draylar.goml.block.augment.*;
import draylar.goml.item.ClaimAnchorBlockItem;
import draylar.goml.item.ToggleableBlockItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
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
    public static final Pair<ClaimAnchorBlock, Item> ADMIN_CLAIM_ANCHOR = register("admin_claim_anchor", () -> -1, -1, GOMLTextures.ADMIN_CLAIM_ANCHOR);

    public static final Pair<ClaimAugmentBlock, Item> ENDER_BINDING = register("ender_binding", new EnderBindingAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.ENDER_BINDING), 2);
    public static final Pair<ClaimAugmentBlock, Item> LAKE_SPIRIT_GRACE = register("lake_spirit_grace", new LakeSpiritGraceAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.LAKE_SPIRIT_GRACE), 2);
    public static final Pair<ClaimAugmentBlock, Item> ANGELIC_AURA = register("angelic_aura", new AngelicAuraAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.ANGELIC_AURA), 2);
    public static final Pair<ClaimAugmentBlock, Item> HEAVEN_WINGS = register("heaven_wings", new HeavenWingsAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.HEAVEN_WINGS), 2);
    public static final Pair<ClaimAugmentBlock, Item> VILLAGE_CORE = register("village_core", new ClaimAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.VILLAGE_CORE), 2);
    public static final Pair<ClaimAugmentBlock, Item> WITHERING_SEAL = register("withering_seal", new WitheringSealAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.WITHERING_SEAL), 2);
    public static final Pair<ClaimAugmentBlock, Item> CHAOS_ZONE = register("chaos_zone", new ChaosZoneAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.CHAOS_ZONE), 2);
    public static final Pair<ClaimAugmentBlock, Item> GREETER = register("greeter", new GreeterAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.GREETER), 2);
    public static final Pair<SelectiveClaimAugmentBlock, Item> PVP_ARENA = register("pvp_arena", new SelectiveClaimAugmentBlock("pvp_arena", FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.PVP_ARENA), 2);
    public static final Pair<ClaimAugmentBlock, Item> EXPLOSION_CONTROLLER = register("explosion_controller", new ExplosionControllerAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.EXPLOSION_CONTROLLER), 2);

    public static final Pair<ClaimAugmentBlock, Item> FORCE_FIELD = register("force_field", new ForceFieldAugmentBlock(FabricBlockSettings.create().hardness(10).resistance(3600000.0F), GOMLTextures.FORCE_FIELD), 2);

    private static Pair<ClaimAnchorBlock, Item> register(String name, IntSupplier radius, float hardness, String texture) {
        var claimAnchorBlock = Registry.register(
                    Registries.BLOCK,
                    GetOffMyLawn.id(name),
                    new ClaimAnchorBlock(FabricBlockSettings.create().strength(hardness, 3600000.0F), radius, texture)
            );


        var registeredItem = Registry.register(Registries.ITEM, GetOffMyLawn.id(name), new ClaimAnchorBlockItem(claimAnchorBlock, new Item.Settings(), 0));
        ANCHORS.add(claimAnchorBlock);
        return Pair.of(claimAnchorBlock, registeredItem);
    }

    private static <T extends ClaimAugmentBlock> Pair<T, Item> register(String name, T augment) {
        return register(name, augment, 0);
    }

    private static <T extends ClaimAugmentBlock> Pair<T, Item> register(String name, T augment, int tooltipLines) {
        return register(name, augment, tooltipLines, true);
    }

    private static <T extends ClaimAugmentBlock> Pair<T, Item> register(String name, T augment, int tooltipLines, boolean withGroup) {
        var id = GetOffMyLawn.id(name);
        BooleanSupplier check = () -> GetOffMyLawn.CONFIG.enabledAugments.getOrDefault(id, true);
        ClaimAugmentBlock registered = Registry.register(
                Registries.BLOCK,
                id,
                augment
        );

        augment.setEnabledCheck(check);

        Item registeredItem = Registry.register(Registries.ITEM, id, new ToggleableBlockItem(augment, new Item.Settings(), tooltipLines, check));
        AUGMENTS.add(registered);

        GOMLAugments.register(id, augment);

        return Pair.of(augment, registeredItem);
    }

    public static void init() {
        // NO-OP
    }

    private GOMLBlocks() {
        // NO-OP
    }
}
