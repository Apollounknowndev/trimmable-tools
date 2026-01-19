package dev.worldgen.trimmable.tools.config;

import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.tag.ClientTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Set;

public class ToolTags {
    public static final Identifier UNKNOWN = TrimmableTools.id("unknown");

    private static Set<Identifier> tag(TagKey<Item> tag) {
        return ClientTags.getOrCreateLocalTag(tag);
    }

    public static Identifier getToolType(Identifier itemId) {
        for (Map.Entry<Identifier, TagKey<Item>> toolType : ConfigHandler.config().toolTypes().entrySet()) {
            if (tag(toolType.getValue()).contains(itemId)) return toolType.getKey();
        }
        return UNKNOWN;
    }
}
