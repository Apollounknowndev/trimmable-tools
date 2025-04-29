package dev.worldgen.trimmable.tools.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import dev.worldgen.trimmable.tools.config.ToolTags;
import dev.worldgen.trimmable.tools.config.TrimData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TrimmableToolsResourceHelper {
    private static final Gson GSON = new Gson();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");

    public static void addAllTrimOverrides(Map<ResourceLocation, Resource> models) {
        for (Map.Entry<ResourceLocation, Resource> entry : new HashSet<>(models.entrySet())) {
            ResourceLocation rawId = entry.getKey();
            ResourceLocation prefixedId = MODEL_LISTER.fileToId(rawId);
            ResourceLocation itemId = TrimmableTools.id(prefixedId.getNamespace(), prefixedId.getPath().substring(5));

            ResourceLocation toolType = ToolTags.getToolType(itemId);
            if (toolType.equals(ToolTags.UNKNOWN)) continue;

            try (Reader reader = entry.getValue().openAsReader()) {

                JsonObject json = GsonHelper.parse(reader);
                String parent = GsonHelper.getAsString(json, "parent");
                JsonObject textures = GsonHelper.getAsJsonObject(json, "textures");
                String layer0 = GsonHelper.getAsString(textures, "layer0");

                for (String pattern : TrimData.patterns()) {
                    for (String rawMaterial : TrimData.materials()) {
                        String material = ConfigHandler.getMaterialName(itemId, rawMaterial);

                        ResourceLocation rawModelId = MODEL_LISTER.idToFile(createTrimmedId(prefixedId, pattern, material));
                        models.put(rawModelId, createTrimModel(entry.getValue().source(), parent, layer0, toolType, pattern, material));
                    }
                }
            } catch (JsonSyntaxException ignored) {

            } catch (Exception e) {
                TrimmableTools.LOGGER.error("Couldn't load trimmable tool data from model {}", rawId, e);
            }
        }
    }

    private static ResourceLocation createTrimmedId(ResourceLocation id, String pattern, String material) {
        return id.withSuffix(String.format("_%s_%s", pattern, material));
    }

    private static Resource createTrimModel(PackResources pack, String parent, String layer0, ResourceLocation toolType, String pattern, String material) {
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        textures.addProperty("layer1", String.format("%s:trims/items/%s/%s_%s", toolType.getNamespace(), toolType.getPath(), pattern, material));

        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        json.add("textures", textures);

        return new Resource(pack, () -> IOUtils.toInputStream(GSON.toJson(json), "UTF-8"));
    }

    public static TrimmedItemModel.Unbaked createItemModel(ResourceLocation itemId, ItemModel.Unbaked wrapped) {
        Object2ObjectMap<ClientTrim, ItemModel.Unbaked> cases = new Object2ObjectOpenHashMap<>();

        for (ResourceLocation material : TrimData.MATERIALS) {
            for (ResourceLocation pattern : TrimData.PATTERNS) {
                ResourceLocation id = createTrimmedId(itemId.withPrefix("item/"), pattern.getPath(), ConfigHandler.getMaterialName(itemId, material.getPath()));
                cases.put(new ClientTrim(material, pattern), new BlockModelWrapper.Unbaked(
                    id,
                    List.of()
                ));
            }
        }

        return new TrimmedItemModel.Unbaked(cases, itemId, wrapped);
    }
}
