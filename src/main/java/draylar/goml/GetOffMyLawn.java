package draylar.goml;

import com.jamieswhiteshirt.rtree3i.Box;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import draylar.goml.api.Claim;
import draylar.goml.api.GomlProtectionProvider;
import draylar.goml.cca.ClaimComponent;
import draylar.goml.cca.WorldClaimComponent;
import draylar.goml.other.CardboardWarning;
import draylar.goml.other.ClaimCommand;
import draylar.goml.config.GOMLConfig;
import draylar.goml.other.PlaceholdersReg;
import draylar.goml.registry.GOMLBlocks;
import draylar.goml.registry.GOMLEntities;
import draylar.goml.registry.GOMLItems;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetOffMyLawn implements ModInitializer, WorldComponentInitializer {

    public static final ComponentKey<ClaimComponent> CLAIM = ComponentRegistryV3.INSTANCE.getOrCreate(id("claims"), ClaimComponent.class);
    public static final PolymerItemGroup GROUP = PolymerItemGroup.create(id("group"), Text.translatable("itemGroup.goml.group"));
    public static final Logger LOGGER = LogManager.getLogger();
    public static GOMLConfig CONFIG = new GOMLConfig();

    public static Identifier id(String name) {
        return new Identifier("goml", name);
    }

    @Override
    public void onInitialize() {
        CardboardWarning.checkAndAnnounce();
        GOMLBlocks.init();
        GOMLItems.init();
        GOMLEntities.init();
        EventHandlers.init();
        ClaimCommand.init();
        PlaceholdersReg.init();

        CommonProtection.register(new Identifier("goml", "claim_protection"), GomlProtectionProvider.INSTANCE);

        ServerLifecycleEvents.SERVER_STARTING.register((s) -> {
            CardboardWarning.checkAndAnnounce();
            GetOffMyLawn.CONFIG = GOMLConfig.loadOrCreateConfig();
        });

        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            CLAIM.get(world).getClaims().values().forEach(x -> {
                x.tick(world);
            });
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            CLAIM.get(world).getClaims().entries().filter(x -> {
                var minX = ChunkSectionPos.getSectionCoord(x.getKey().toBox().x1());
                var minZ = ChunkSectionPos.getSectionCoord(x.getKey().toBox().z1());

                var maxX = ChunkSectionPos.getSectionCoord(x.getKey().toBox().x2());
                var maxZ = ChunkSectionPos.getSectionCoord(x.getKey().toBox().z2());

                return (minX <= chunk.getPos().x && maxX >= chunk.getPos().x && minZ <= chunk.getPos().z && maxZ >= chunk.getPos().z);
            }).forEach(x -> x.getValue().internal_incrementChunks());
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            CLAIM.get(world).getClaims().entries().filter(x -> {
                var minX = ChunkSectionPos.getSectionCoord(x.getKey().toBox().x1());
                var minZ = ChunkSectionPos.getSectionCoord(x.getKey().toBox().z1());

                var maxX = ChunkSectionPos.getSectionCoord(x.getKey().toBox().x2());
                var maxZ = ChunkSectionPos.getSectionCoord(x.getKey().toBox().z2());

                return (minX <= chunk.getPos().x && maxX >= chunk.getPos().x && minZ <= chunk.getPos().z && maxZ >= chunk.getPos().z);
            }).forEach(x -> x.getValue().internal_decrementChunks());
        });

        GROUP.setIcon(new ItemStack(GOMLBlocks.WITHERED_CLAIM_ANCHOR.getSecond()));
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(CLAIM, WorldClaimComponent::new);
    }
}
