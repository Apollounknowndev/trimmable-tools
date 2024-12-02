package dev.worldgen.trimmable.tools.resource;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import dev.worldgen.trimmable.tools.config.TrimData;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public record TrimPalettedPermutations(List<ResourceLocation> textures, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations) implements SpriteSource {
    public static final Codec<TrimPalettedPermutations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("palette_key").forGetter(TrimPalettedPermutations::paletteKey)
    ).apply(instance, TrimPalettedPermutations::create));

    public static TrimPalettedPermutations create(ResourceLocation paletteKey) {
        List<ResourceLocation> textures = new ArrayList<>();
        for (ResourceLocation toolType : ConfigHandler.config().toolTypes().keySet()) {
            for (ResourceLocation pattern : TrimData.PATTERNS) {
                textures.add(toolType.withPrefix("trims/items/").withSuffix("/"+pattern.getPath()));
            }
        }

        Map<String, ResourceLocation> permutations = new HashMap<>();
        for (ResourceLocation id : TrimData.MATERIALS) {
            String key = id.getPath();
            ResourceLocation entry = id.withPrefix("trims/color_palettes/");

            permutations.put(key, entry);

            if (ConfigHandler.hasDarkerVariant(key)) {
                permutations.put(key + "_darker", entry.withSuffix("_darker"));
            }
        }

        return new TrimPalettedPermutations(textures, paletteKey, permutations);
    }

    public void run(@NotNull ResourceManager manager, @NotNull SpriteSource.Output output) {
        Supplier<int[]> supplier = Suppliers.memoize(() -> loadPaletteEntryFromImage(manager, this.paletteKey));
        Map<String, Supplier<IntUnaryOperator>> map = new HashMap<>();
        this.permutations.forEach((p_267108_, p_266969_) -> map.put(p_267108_, Suppliers.memoize(() -> createPaletteMapping(supplier.get(), loadPaletteEntryFromImage(manager, p_266969_)))));

        for (ResourceLocation textureId : this.textures) {
            ResourceLocation textureFileId = TEXTURE_ID_CONVERTER.idToFile(textureId);
            Optional<Resource> optional = manager.getResource(textureFileId);
            if (optional.isEmpty()) {
                TrimmableTools.LOGGER.warn("Unable to find texture {}", textureFileId);
            } else {
                LazyLoadedImage lazyloadedimage = new LazyLoadedImage(textureFileId, optional.get(), map.size());

                for (Map.Entry<String, Supplier<IntUnaryOperator>> entry : map.entrySet()) {
                    ResourceLocation palettedId = textureId.withSuffix("_" + entry.getKey());
                    output.add(palettedId, new PalettedSpriteSupplier(lazyloadedimage, entry.getValue(), palettedId));
                }
            }
        }
    }

    private static IntUnaryOperator createPaletteMapping(int[] p_266839_, int[] p_266776_) {
        if (p_266776_.length != p_266839_.length) {
            TrimmableTools.LOGGER.warn("Palette mapping has different sizes: {} and {}", p_266839_.length, p_266776_.length);
            throw new IllegalArgumentException();
        } else {
            Int2IntMap int2intmap = new Int2IntOpenHashMap(p_266776_.length);

            for(int i = 0; i < p_266839_.length; ++i) {
                int j = p_266839_[i];
                if (FastColor.ABGR32.alpha(j) != 0) {
                    int2intmap.put(FastColor.ABGR32.transparent(j), p_266776_[i]);
                }
            }

            return (p_267899_) -> {
                int k = FastColor.ABGR32.alpha(p_267899_);
                if (k == 0) {
                    return p_267899_;
                } else {
                    int l = FastColor.ABGR32.transparent(p_267899_);
                    int i1 = int2intmap.getOrDefault(l, FastColor.ABGR32.opaque(l));
                    int j1 = FastColor.ABGR32.alpha(i1);
                    return FastColor.ABGR32.color(k * j1 / 255, i1);
                }
            };
        }
    }

    public static int[] loadPaletteEntryFromImage(ResourceManager manager, ResourceLocation palette) {
        Optional<Resource> optional = manager.getResource(TEXTURE_ID_CONVERTER.idToFile(palette));
        if (optional.isEmpty()) {
            TrimmableTools.LOGGER.error("Failed to load palette image {}", palette);
            throw new IllegalArgumentException();
        } else {
            try {
                InputStream inputstream = optional.get().open();

                int[] pixels;
                try {
                    NativeImage nativeimage = NativeImage.read(inputstream);

                    try {
                        pixels = nativeimage.getPixelsRGBA();
                    } catch (Throwable var10) {
                        try {
                            nativeimage.close();
                        } catch (Throwable var9) {
                            var10.addSuppressed(var9);
                        }

                        throw var10;
                    }

                    nativeimage.close();
                } catch (Throwable var11) {
                    try {
                        inputstream.close();
                    } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                    }

                    throw var11;
                }

                inputstream.close();

                return pixels;
            } catch (Exception var12) {
                TrimmableTools.LOGGER.error("Couldn't load texture {}", palette, var12);
                throw new IllegalArgumentException();
            }
        }
    }

    @NotNull
    public SpriteSourceType type() {
        return SpriteSources.PALETTED_PERMUTATIONS;
    }

    record PalettedSpriteSupplier(LazyLoadedImage baseImage, Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) implements SpriteSupplier {

        public SpriteContents get() {
            try {
                NativeImage nativeimage = this.baseImage.get().mappedCopy(this.palette.get());
                return new SpriteContents(this.permutationLocation, new FrameSize(nativeimage.getWidth(), nativeimage.getHeight()), nativeimage, AnimationMetadataSection.EMPTY);
            } catch (IOException | IllegalArgumentException var8) {
                TrimmableTools.LOGGER.error("Unable to apply palette to {}", this.permutationLocation, var8);
            } finally {
                this.baseImage.release();
            }

            return null;
        }

        public void discard() {
            this.baseImage.release();
        }
    }
}
