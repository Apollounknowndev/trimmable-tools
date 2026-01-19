package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.minecraft.resources.Identifier;

public class TrimmableToolsClient {
    public static final Identifier TRIM_PATTERN = TrimmableTools.id("trim_pattern");
    public static final Identifier TRIM_MATERIAL = TrimmableTools.id("trim_material");
    public static void init() {
        ConfigHandler.load();
    }
}