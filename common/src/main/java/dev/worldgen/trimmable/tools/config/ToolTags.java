package dev.worldgen.trimmable.tools.config;

import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.tag.ClientTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Set;

public class ToolTags {
    public static final ResourceLocation UNKNOWN = TrimmableTools.id("unknown");

    private static Set<ResourceLocation> tag(TagKey<Item> tag) {
        return ClientTags.getOrCreateLocalTag(tag);
    }

    public static ResourceLocation getToolType(ResourceLocation itemId) {
        for (Map.Entry<ResourceLocation, TagKey<Item>> toolType : ConfigHandler.config().toolTypes().entrySet()) {
            if (tag(toolType.getValue()).contains(itemId)) return toolType.getKey();
        }
        return UNKNOWN;
    }
}
