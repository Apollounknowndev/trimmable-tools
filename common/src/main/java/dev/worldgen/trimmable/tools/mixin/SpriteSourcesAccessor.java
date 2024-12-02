package dev.worldgen.trimmable.tools.mixin;

import com.google.common.collect.BiMap;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.resource.TrimPalettedPermutations;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This is an accessor on later versions but has to be a regular mixin due to Lexforge.
 * Will all of my heart: I. Despise. Forge.
 */
@Mixin(SpriteSources.class)
public class SpriteSourcesAccessor {
    @Shadow
    @Final
    private static BiMap<ResourceLocation, SpriteSourceType> TYPES;

    static {
        TYPES.put(TrimmableTools.id("paletted_permutations"), new SpriteSourceType(TrimPalettedPermutations.CODEC));
    }
}
