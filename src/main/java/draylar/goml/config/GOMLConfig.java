package draylar.goml.config;


import com.google.gson.*;
import com.ibm.icu.impl.ValidIdentifiers;
import draylar.goml.GetOffMyLawn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GOMLConfig {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping().setLenient().setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .create();


    public List<String> dimensionBlacklist = new ArrayList<>();
    public int makeshiftRadius = 10;
    public int reinforcedRadius = 25;
    public int glisteningRadius = 50;
    public int crystalRadius = 75;
    public int emeradicRadius = 125;
    public int witheredRadius = 200;

    public Set<Identifier> allowedBlockInteraction = new HashSet<>();

    public Set<Identifier> allowedEntityInteraction = new HashSet<>();

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


    private static class IdentifierSerializer implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {

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
}
