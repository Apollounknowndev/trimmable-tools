package dev.worldgen.trimmable.tools.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsClient;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrimmableToolsResourceHelper {
    private static final Gson GSON = new Gson();
    private static final String TOOL_TYPE_KEY = "trimmable_tools:tool_type";
    private static final String MATERIAL_KEY = "trimmable_tools:material";

    // TODO: Cleanup code
    public static void addAllTrimOverrides(Map<ResourceLocation, Resource> models) {
        for (Map.Entry<ResourceLocation, Resource> entry : new HashSet<>(models.entrySet())) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);

                if (!isTrimmableTool(json)) continue;
                String toolType = GsonHelper.getAsString(json, TOOL_TYPE_KEY);
                String toolMaterial = getToolMaterial(json);

                String parent = getParent(json);
                String layer0 = getLayer0(json);
                JsonArray overrides = getOverrides(json);

                List<ResourceLocation> patterns = ConfigHandler.patterns();
                for (int i = 0; i < patterns.size(); i++) {
                    String pattern = patterns.get(i).getPath();

                    List<ResourceLocation> materials = ConfigHandler.materials();
                    for (int j = 0; j < materials.size(); j++) {
                        String material = materials.get(j).getPath();
                        if (Objects.equals(material, toolMaterial)) {
                            material = material + "_darker";
                        }

                        ResourceLocation rawModelId = createTrimmedToolId(entry.getKey(), pattern, material);
                        models.put(rawModelId, createTrimOverrideResource(entry.getValue().source(), parent, layer0, toolType, pattern, material));

                        JsonObject override = new JsonObject();
                        override.addProperty("model", createModelId(rawModelId));

                        JsonObject predicate = new JsonObject();
                        predicate.addProperty(TrimmableToolsClient.TRIM_PATTERN.toString(), (float) (i + 1) / 1000);
                        predicate.addProperty(TrimmableToolsClient.TRIM_MATERIAL.toString(), (float) (j + 1) / 1000);
                        override.add("predicate", predicate);

                        overrides.add(override);
                    }

                    json.add("overrides", overrides);
                    models.put(entry.getKey(), new Resource(entry.getValue().source(), createSupplier(json)));
                }
            } catch (JsonSyntaxException ignored) {

            } catch (Exception e) {
                TrimmableTools.LOGGER.error("Couldn't load trimmable tool data from model {}", entry.getKey(), e);
            }
        }
    }

    private static boolean isTrimmableTool(JsonObject json) {
        return json.has(TOOL_TYPE_KEY) && GsonHelper.isStringValue(json.get(TOOL_TYPE_KEY));
    }

    private static String getToolMaterial(JsonObject json) {
        String toolMaterial = "";
        if (json.has(MATERIAL_KEY) && GsonHelper.isStringValue(json.get(MATERIAL_KEY))) {
            toolMaterial = GsonHelper.getAsString(json, MATERIAL_KEY);
        }
        return toolMaterial;
    }

    private static ResourceLocation createTrimmedToolId(ResourceLocation id, String pattern, String material) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(".json", String.format("_%s_%s.json", pattern, material)));
    }

    private static String createModelId(ResourceLocation id) {
        String path = id.getPath();
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path.substring(7, path.length() - 5)).toString();
    }

    private static Resource createTrimOverrideResource(PackResources pack, String parent, String layer0, String trimType, String pattern, String material) {
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        textures.addProperty("layer1", String.format("trimmable_tools:trims/items/%s/%s_%s", trimType, pattern, material));

        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        json.add("textures", textures);

        return new Resource(pack, createSupplier(json));
    }

    private static String getParent(JsonObject json) {
        return GsonHelper.getAsString(json, "parent");
    }

    private static String getLayer0(JsonObject json) {
        JsonObject textures = GsonHelper.getAsJsonObject(json, "textures");
        return GsonHelper.getAsString(textures, "layer0");
    }


    private static JsonArray getOverrides(JsonObject json) {
        try {
            return GsonHelper.getAsJsonArray(json, "overrides");
        } catch (Exception e) {
            return new JsonArray();
        }
    }

    private static IoSupplier<InputStream> createSupplier(JsonObject json) {
        return () -> IOUtils.toInputStream(GSON.toJson(json), "UTF-8");
    }
}
