package dev.worldgen.trimmable.tools.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsClient;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class TrimmableToolsResourceHelper {
    private static final Gson GSON = new Gson();
    private static final String TOOL_TYPE_KEY = "trimmable_tools:tool_type";
    private static final String MATERIAL_KEY = "trimmable_tools:material";

    // TODO: Cleanup code
    public static void addAllTrimOverrides(Map<Identifier, Resource> resources) {
        for (Map.Entry<Identifier, Resource> entry : new HashSet<>(resources.entrySet())) {
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject json = JsonHelper.deserialize(reader);

                if (!isTrimmableTool(json)) continue;
                String toolType = JsonHelper.getString(json, TOOL_TYPE_KEY);
                String toolMaterial = getToolMaterial(json);

                Optional<String> optionalParent = getParent(json);
                Optional<String> optionalLayer0 = getLayer0(json);
                Optional<JsonArray> optionalOverrides = getOverrides(json);

                if (optionalParent.isPresent() && optionalLayer0.isPresent() && optionalOverrides.isPresent()) {
                    String parent = optionalParent.get();
                    String layer0 = optionalLayer0.get();
                    JsonArray overrides = optionalOverrides.get();

                    List<Identifier> patterns = ConfigHandler.patterns();
                    for (int i = 0; i < patterns.size(); i++) {
                        String pattern = patterns.get(i).getPath();

                        List<Identifier> materials = ConfigHandler.materials();
                        for (int j = 0; j < materials.size(); j++) {
                            String material = materials.get(j).getPath();
                            if (Objects.equals(material, toolMaterial)) {
                                material = material + "_darker";
                            }

                            Identifier rawModelId = createRawModelId(entry.getKey(), pattern, material);
                            resources.put(rawModelId, createTrimOverrideResource(entry.getValue().getPack(), parent, layer0, toolType, pattern, material));

                            JsonObject override = new JsonObject();
                            override.addProperty("model", createModelId(rawModelId));

                            JsonObject predicate = new JsonObject();
                            predicate.addProperty(TrimmableToolsClient.TRIM_PATTERN.toString(), (float) (i + 1) / 1000);
                            predicate.addProperty(TrimmableToolsClient.TRIM_MATERIAL.toString(), (float) (j + 1) / 1000);
                            override.add("predicate", predicate);

                            overrides.add(override);
                        }
                    }

                    json.add("overrides", overrides);
                    resources.put(entry.getKey(), new Resource(entry.getValue().getPack(), createSupplier(json)));
                }
            } catch (Exception e) {
                TrimmableTools.LOGGER.error("Couldn't load trimmable tool data from model {}", entry.getKey(), e);
            }
        }
    }

    private static boolean isTrimmableTool(JsonObject json) {
        return json.has(TOOL_TYPE_KEY) && JsonHelper.isString(json.get(TOOL_TYPE_KEY));
    }

    private static String getToolMaterial(JsonObject json) {
        String toolMaterial = "";
        if (json.has(MATERIAL_KEY) && JsonHelper.isString(json.get(MATERIAL_KEY))) {
            toolMaterial = JsonHelper.getString(json, MATERIAL_KEY);
        }
        return toolMaterial;
    }

    private static Identifier createRawModelId(Identifier id, String pattern, String material) {
        return Identifier.of(id.getNamespace(), id.getPath().replace(".json", String.format("_%s_%s.json", pattern, material)));
    }

    private static String createModelId(Identifier id) {
        String path = id.getPath();
        return Identifier.of(id.getNamespace(), path.substring(7, path.length() - 5)).toString();
    }

    private static Resource createTrimOverrideResource(ResourcePack pack, String parent, String layer0, String trimType, String pattern, String material) {
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        textures.addProperty("layer1", String.format("trimmable_tools:trims/items/%s/%s_%s", trimType, pattern, material));

        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        json.add("textures", textures);

        return new Resource(pack, createSupplier(json));
    }

    private static Optional<String> getParent(JsonObject json) {
        try {
            String parent = JsonHelper.getString(json, "parent");
            return Optional.of(parent);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<String> getLayer0(JsonObject json) {
        try {
            JsonObject textures = JsonHelper.getObject(json, "textures");
            String layer0 = JsonHelper.getString(textures, "layer0");
            return Optional.of(layer0);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<JsonArray> getOverrides(JsonObject json) {
        try {
            JsonArray array = JsonHelper.getArray(json, "overrides");
            return Optional.of(array);
        } catch (Exception e) {
            return Optional.of(new JsonArray());
        }
    }

    private static InputSupplier<InputStream> createSupplier(JsonObject json) {
        return () -> IOUtils.toInputStream(GSON.toJson(json), "UTF-8");
    }
}
