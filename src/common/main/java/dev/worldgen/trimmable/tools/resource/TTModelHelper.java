package dev.worldgen.trimmable.tools.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsHelper;
import dev.worldgen.trimmable.tools.resource.data.TTClientData;
import dev.worldgen.trimmable.tools.resource.data.TTClientData.MaterialData;
import dev.worldgen.trimmable.tools.resource.data.TTClientData.PatternData;
import dev.worldgen.trimmable.tools.resource.data.TTClientDataManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TTModelHelper {
    private static final Gson GSON = new Gson();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    private static final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");
    
    public static void addAllTrimOverrides(ResourceManager manager, Map<Identifier, Resource> models) {
        TTClientData data = TTClientDataManager.INSTANCE.getClientData();
        for (Map.Entry<Identifier, Resource> entry : new HashSet<>(models.entrySet())) {
            Identifier rawId = entry.getKey();
            Identifier prefixedId = MODEL_LISTER.fileToId(rawId);
            Identifier itemId = TrimmableTools.id(prefixedId.getNamespace(), prefixedId.getPath().substring(5));

            Optional<Identifier> toolType = data.getToolType(itemId);
            if (toolType.isEmpty()) continue;

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                String parent = GsonHelper.getAsString(json, "parent");
                JsonObject textures = GsonHelper.getAsJsonObject(json, "textures");
                String layer0 = GsonHelper.getAsString(textures, "layer0");

                for (PatternData pattern : data.patterns()) {
                    
                    for (MaterialData rawMaterial : data.materials()) {
                        Identifier material = rawMaterial.getMaterial(itemId);

                        Identifier rawModelId = MODEL_LISTER.idToFile(createTrimmedId(prefixedId, pattern, material));
                        models.put(rawModelId, createTrimModel(manager, entry.getValue().source(), parent, layer0, toolType.get(), pattern, material));
                    }
                }
            } catch (JsonSyntaxException ignored) {

            } catch (Exception e) {
                TrimmableTools.LOGGER.error("Couldn't load trimmable tool data from model {}", rawId, e);
            }
        }
    }

    private static Identifier createTrimmedId(Identifier id, PatternData pattern, Identifier material) {
        return id.withSuffix(String.format("_%s_%s", pattern.id().getPath(), material.getPath()));
    }

    private static Resource createTrimModel(ResourceManager manager, PackResources pack, String parent, String layer0, Identifier toolType, PatternData pattern, Identifier material) {
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        
        Identifier textureId = TEXTURE_ID_CONVERTER.idToFile(TrimmableToolsHelper.getTrimPermutationId(toolType, pattern.id(), null));
        Optional<Resource> resource = manager.getResource(textureId);
        if (resource.isPresent()) {
            textures.addProperty("layer1", TrimmableToolsHelper.getTrimPermutationId(toolType, pattern.id(), material).toString());
        }

        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        json.add("textures", textures);

        return new Resource(pack, () -> IOUtils.toInputStream(GSON.toJson(json), "UTF-8"));
    }

    public static TrimmedItemModel.Unbaked createItemModel(Identifier itemId, ItemModel.Unbaked wrapped) {
        TTClientData data = TTClientDataManager.INSTANCE.getClientData();
        Object2ObjectMap<ClientTrim, ItemModel.Unbaked> cases = new Object2ObjectOpenHashMap<>();

        for (MaterialData material : data.materials()) {
            for (PatternData pattern : data.patterns()) {
                Identifier id = createTrimmedId(itemId.withPrefix("item/"), pattern, material.getMaterial(itemId));
                cases.put(new ClientTrim(material.id(), pattern.id()), new BlockModelWrapper.Unbaked(
                    id,
                    List.of()
                ));
            }
        }

        return new TrimmedItemModel.Unbaked(cases, itemId, wrapped);
    }
}
