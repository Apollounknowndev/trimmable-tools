package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import dev.worldgen.trimmable.tools.mixin.SpriteSourcesAccessor;
import dev.worldgen.trimmable.tools.resource.TrimPalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.Map;

public class TrimmableToolsClient {
    public static final ResourceLocation TRIM_PATTERN = TrimmableTools.id("trim_pattern");
    public static final ResourceLocation TRIM_MATERIAL = TrimmableTools.id("trim_material");
    public static void init() {
        ConfigHandler.load();
    }
}