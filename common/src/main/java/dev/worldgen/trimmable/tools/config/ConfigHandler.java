package dev.worldgen.trimmable.tools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.platform.Services;
import dev.worldgen.trimmable.tools.resource.TrimmableToolsResourceHelper;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ConfigHandler {
    private static final Path CONFIG_PATH = Services.PLATFORM.getConfigFolder().resolve("trimmable_tools.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TrimDataConfig config = TrimDataConfig.DEFAULT;

    public static void load() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            writeDefault();
        }
        try {
            JsonElement json = JsonParser.parseString(new String(Files.readAllBytes(CONFIG_PATH)));
            var dataResult = TrimDataConfig.CODEC.parse(JsonOps.INSTANCE, json);
            dataResult.ifError(error -> {
                TrimmableTools.LOGGER.error("Config file has missing or invalid data: "+error.message());
                writeDefault();
            });
            if (dataResult.result().isPresent()) {
                config = dataResult.result().get();
            }
        } catch (IOException e) {
            TrimmableTools.LOGGER.error("Malformed json in config file found, default config will be used");
            writeDefault();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TrimDataConfig config() {
        return config;
    }

    public static String getDarkerMaterial(ResourceLocation modelId) {
        ResourceLocation id = TrimmableToolsResourceHelper.stripModelAffixes(modelId);
        for (Map.Entry<String, List<ResourceLocation>> entry : config.darkerMaterials().entrySet()) {
            if (entry.getValue().contains(id)) {
                return entry.getKey();
            }
        }
        return "";
    }

    public static boolean hasDarkerVariant(String name) {
        return config.darkerMaterials().keySet().stream().anyMatch(key -> key.equals(name));
    }

    private static void writeDefault() {
        try(BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            JsonElement json = TrimDataConfig.CODEC.encodeStart(JsonOps.INSTANCE, TrimDataConfig.DEFAULT).getOrThrow();
            writer.write(GSON.toJson(json));
        } catch (Exception e) {
            TrimmableTools.LOGGER.error("Couldn't write default config to file", e);
        }
    }
}