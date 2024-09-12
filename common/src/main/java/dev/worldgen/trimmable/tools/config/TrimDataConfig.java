package dev.worldgen.trimmable.tools.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

import java.util.List;
import java.util.stream.Collectors;

public record TrimDataConfig(List<ResourceLocation> patterns, List<ResourceLocation> materials) {
    public static final Codec<List<ResourceLocation>> CAPPED_ID_LIST_CODEC = ExtraCodecs.validate(
        ResourceLocation.CODEC.listOf(),
        ids -> ids.size() > 999 ?
        DataResult.error(() -> String.format("Max list entries is 999, but list has %s entries! Why do you need that many, anyways?", ids.size())) :
        DataResult.success(ids)
    );

    public static final Codec<TrimDataConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CAPPED_ID_LIST_CODEC.fieldOf("patterns").forGetter(TrimDataConfig::patterns),
        CAPPED_ID_LIST_CODEC.fieldOf("materials").forGetter(TrimDataConfig::materials)
    ).apply(instance, TrimDataConfig::new));

    public static TrimDataConfig create(List<ResourceKey<TrimPattern>> patterns, List<ResourceKey<TrimMaterial>> materials) {
        return new TrimDataConfig(patterns.stream().map(ResourceKey::location).collect(Collectors.toList()), materials.stream().map(ResourceKey::location).collect(Collectors.toList()));
    }
}
