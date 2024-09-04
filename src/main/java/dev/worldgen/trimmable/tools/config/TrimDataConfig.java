package dev.worldgen.trimmable.tools.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public record TrimDataConfig(List<Identifier> patterns, List<Identifier> materials) {
    public static final Codec<List<Identifier>> CAPPED_ID_LIST_CODEC = Identifier.CODEC.listOf().validate(
        ids -> ids.size() > 999 ?
        DataResult.error(() -> String.format("Max list entries is 999, but list has %s entries! Why do you need that many, anyways?", ids.size())) :
        DataResult.success(ids)
    );

    public static final Codec<TrimDataConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CAPPED_ID_LIST_CODEC.fieldOf("patterns").forGetter(TrimDataConfig::patterns),
        CAPPED_ID_LIST_CODEC.fieldOf("materials").forGetter(TrimDataConfig::materials)
    ).apply(instance, TrimDataConfig::new));

    public static TrimDataConfig create(List<RegistryKey<ArmorTrimPattern>> patterns, List<RegistryKey<ArmorTrimMaterial>> materials) {
        return new TrimDataConfig(patterns.stream().map(RegistryKey::getValue).collect(Collectors.toList()), materials.stream().map(RegistryKey::getValue).collect(Collectors.toList()));
    }
}
