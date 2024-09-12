package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;

public class TrimmableToolsClient {
    public static final ResourceLocation TRIM_PATTERN = TrimmableTools.id("trim_pattern");
    public static final ResourceLocation TRIM_MATERIAL = TrimmableTools.id("trim_material");
    public static void init() {
        ConfigHandler.load();
    }
}