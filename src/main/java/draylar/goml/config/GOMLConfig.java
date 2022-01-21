package draylar.goml.config;


import com.google.gson.*;
import com.jamieswhiteshirt.rtree3i.Box;
import draylar.goml.GetOffMyLawn;
import draylar.goml.api.ClaimBox;
import draylar.goml.other.WrappedText;
import draylar.goml.registry.GOMLBlocks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GOMLConfig {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping().setLenient().setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(WrappedText.class, new WrappedTextSerializer())
            .create();


    public int makeshiftRadius = 10;
    public int reinforcedRadius = 25;
    public int glisteningRadius = 50;
    public int crystalRadius = 75;
    public int emeradicRadius = 125;
    public int witheredRadius = 200;

    public boolean claimProtectsFullWorldHeight = false;

    public Set<Identifier> dimensionBlacklist = new HashSet<>();
    public Map<Identifier, List<Box>> regionBlacklist = new HashMap<>();

    public Map<Identifier, Boolean> enabledAugments = new HashMap<>();

    public Set<Identifier> allowedBlockInteraction = new HashSet<>();

    public Set<Identifier> allowedEntityInteraction = new HashSet<>();

    public WrappedText messagePrefix = WrappedText.of("<dark_gray>[<#a1ff59>GOML</color>]");

    public WrappedText placeholderNoClaimInfo = WrappedText.of("<gray><italic>Wilderness");
    public WrappedText placeholderNoClaimOwners = WrappedText.of("<gray><italic>Nobody");
    public WrappedText placeholderNoClaimTrusted = WrappedText.of("<gray><italic>Nobody");
    public WrappedText placeholderClaimCanBuildInfo = WrappedText.of("${owners} <gray>(<green>${anchor}</green>)");
    public WrappedText placeholderClaimCantBuildInfo = WrappedText.of("${owners} <gray>(<red>${anchor}</red>)");

    public boolean canInteract(Block block) {
        return this.allowedBlockInteraction.contains(Registry.BLOCK.getId(block));
    }

    public boolean canInteract(Entity entity) {
        return this.allowedEntityInteraction.contains(Registry.ENTITY_TYPE.getId(entity.getType()));
    }

    public boolean isBlacklisted(World world, Box claimBox) {
        if (this.dimensionBlacklist.contains(world.getRegistryKey().getValue())) {
            return true;
        }

        var list = this.regionBlacklist.get(world.getRegistryKey().getValue());
        if (list != null) {
            for (var box : list) {
                if (box.intersectsClosed(claimBox)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static GOMLConfig loadOrCreateConfig() {
        try {
            GOMLConfig config;
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "getoffmylawn.json");

            if (configFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));

                config = GSON.fromJson(json, GOMLConfig.class);
            } else {
                config = new GOMLConfig();
            }

            for (var augment : GOMLBlocks.AUGMENTS) {
                var id = Registry.BLOCK.getId(augment);

                if (id != null && !config.enabledAugments.containsKey(id)) {
                    config.enabledAugments.put(id, true);
                }
            }

            saveConfig(config);
            return config;
        }
        catch(IOException exception) {
            GetOffMyLawn.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
            return new GOMLConfig();
        }
    }

    public static void saveConfig(GOMLConfig config) {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "getoffmylawn.json");
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(config));
            writer.close();
        } catch (Exception e) {
            GetOffMyLawn.LOGGER.error("Something went wrong while saving config!");
            e.printStackTrace();
        }
    }

    private static final class IdentifierSerializer implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {

        @Override
        public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return Identifier.tryParse(json.getAsString());
            }
            return null;
        }

        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static final class WrappedTextSerializer implements JsonSerializer<WrappedText>, JsonDeserializer<WrappedText> {

        @Override
        public WrappedText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return WrappedText.of(json.getAsString());
            }
            return null;
        }

        @Override
        public JsonElement serialize(WrappedText src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.input());
        }
    }
}
