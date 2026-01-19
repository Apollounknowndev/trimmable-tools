package dev.worldgen.trimmable.tools.resource;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsHelper;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record TTPalettedPermutations() implements SpriteSource {
    public static final MapCodec<TTPalettedPermutations> MAP_CODEC = MapCodec.unit(new TTPalettedPermutations());
    public static final Identifier PALETTE_KEY = Identifier.withDefaultNamespace("trims/color_palettes/trim_palette");
    public static final String DEFAULT_SEPARATOR = "_";

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        // Collect, parse, and merge all trimmable tool data
        TrimmableToolData data = TrimmableToolData.EMPTY;
        for (String namespace : manager.getNamespaces()) {
            var resource = manager.getResource(TrimmableTools.id(namespace, "trimmable_tools.json"));
            if (resource.isEmpty()) continue;

            try {
                BufferedReader reader = resource.get().openAsReader();
                var dataResult = TrimmableToolData.CODEC.parse(JsonOps.INSTANCE, LenientJsonParser.parse(reader));
                if (dataResult.isSuccess()) {
                    data = TrimmableToolData.merge(data, dataResult.getOrThrow());
                } else {
                    throw new IllegalStateException(dataResult.error().orElseThrow().message());
                }
            } catch (Exception e) {
                TrimmableTools.LOGGER.warn("Couldn't parse trimmable tools data from {} namespace: {}", namespace, e);
            }
        }

        // Collect pattern textures
        List<Identifier> textures = new ArrayList<>();
        for (Identifier toolType : data.toolTypes().keySet()) {
            for (TrimmableToolData.PatternData pattern : data.patterns()) {
                if (pattern.requiredMod().isEmpty() || TrimmableToolsHelper.isModLoaded(pattern.requiredMod().get())) {
                    textures.add(toolType.withPrefix("trims/items/").withSuffix("/" + pattern.id().getPath()));
                }
            }
        }

        // Collect material permutations
        Map<String, Identifier> permutations = new HashMap<>();
        for (TrimmableToolData.MaterialData material : data.materials()) {
            String key = material.id().getPath();
            Identifier entry = material.id().withPrefix("trims/color_palettes/");

            permutations.put(key, entry);

            if (ConfigHandler.hasDarkerVariant(key)) {
                permutations.put(key + "_darker", entry.withSuffix("_darker"));
            }
        }


        Supplier<int[]> paletteKeySupplier = Suppliers.memoize(() -> TTPalettedPermutations.loadPaletteEntryFromImage(manager, PALETTE_KEY));
        HashMap<String, Supplier<IntUnaryOperator>> palettes = new HashMap<>();
        permutations.forEach((suffix, palette) -> palettes.put(suffix, Suppliers.memoize(() -> TTPalettedPermutations.createPaletteMapping(paletteKeySupplier.get(), TTPalettedPermutations.loadPaletteEntryFromImage(manager, palette)))));
        for (Identifier textureLocation : textures) {
            Identifier textureId = TEXTURE_ID_CONVERTER.idToFile(textureLocation);
            Optional<Resource> resource = manager.getResource(textureId);
            if (resource.isEmpty()) {
                TrimmableTools.LOGGER.warn("Unable to find texture {}", textureId);
                continue;
            }
            LazyLoadedImage baseImage = new LazyLoadedImage(textureId, resource.get(), palettes.size());
            for (var entry : palettes.entrySet()) {
                Identifier permutationLocation = textureLocation.withSuffix(DEFAULT_SEPARATOR + entry.getKey());
                TrimmableTools.LOGGER.warn(permutationLocation.toString());
                output.add(permutationLocation, new TTPalettedPermutations.PalettedSpriteSupplier(baseImage, entry.getValue(), permutationLocation));
            }
        }
    }

    private static IntUnaryOperator createPaletteMapping(int[] keys, int[] values) {
        if (values.length != keys.length) {
            TrimmableTools.LOGGER.warn("Palette mapping has different sizes: {} and {}", keys.length, values.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap palette = new Int2IntOpenHashMap(values.length);
        for (int i = 0; i < keys.length; ++i) {
            int key = keys[i];
            if (ARGB.alpha(key) == 0) continue;
            palette.put(ARGB.transparent(key), values[i]);
        }
        return pixel -> {
            int pixelAlpha = ARGB.alpha(pixel);
            if (pixelAlpha == 0) {
                return pixel;
            }
            int pixelRGB = ARGB.transparent(pixel);
            int value = palette.getOrDefault(pixelRGB, ARGB.opaque(pixelRGB));
            int valueAlpha = ARGB.alpha(value);
            return ARGB.color(pixelAlpha * valueAlpha / 255, value);
        };
    }

    private static int[] loadPaletteEntryFromImage(ResourceManager resourceManager, Identifier location) {
        Optional<Resource> resource = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(location));
        if (resource.isEmpty()) {
            TrimmableTools.LOGGER.error("Failed to load palette image {}", location);
            throw new IllegalArgumentException();
        }
        try (InputStream is = resource.get().open()){
            NativeImage image = NativeImage.read(is);
            try {
                int[] nArray = image.getPixels();
                image.close();
                return nArray;
            } catch (Throwable throwable) {
                try {
                    image.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        } catch (Exception exception) {
            TrimmableTools.LOGGER.error("Couldn't load texture {}", location, exception);
            throw new IllegalArgumentException();
        }
    }

    public MapCodec<TTPalettedPermutations> codec() {
        return MAP_CODEC;
    }

    @Environment(value=EnvType.CLIENT)
    private record PalettedSpriteSupplier(LazyLoadedImage baseImage, java.util.function.Supplier<IntUnaryOperator> palette, Identifier permutationLocation) implements SpriteSource.DiscardableLoader {
        @Override
        public @Nullable SpriteContents get(SpriteResourceLoader loader) {
            try {
                NativeImage image = this.baseImage.get().mappedCopy(this.palette.get());
                return new SpriteContents(this.permutationLocation, new FrameSize(image.getWidth(), image.getHeight()), image);
            } catch (IOException | IllegalArgumentException e) {
                TrimmableTools.LOGGER.error("unable to apply palette to {}", this.permutationLocation, e);
                return null;
            } finally {
                this.baseImage.release();
            }
        }

        @Override
        public void discard() {
            this.baseImage.release();
        }
    }
}


