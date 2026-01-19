package dev.worldgen.trimmable.tools.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.worldgen.trimmable.tools.TrimmableTools.id;

public record TrimDataConfig(Map<Identifier, TagKey<Item>> toolTypes, Map<String, List<Identifier>> darkerMaterials, List<Identifier> extraPatterns, List<Identifier> extraMaterials) {
    public static final Codec<TrimDataConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Identifier.CODEC, TagKey.hashedCodec(Registries.ITEM)).fieldOf("tool_types").forGetter(TrimDataConfig::toolTypes),
        Codec.unboundedMap(Codec.STRING, Identifier.CODEC.listOf()).fieldOf("darker_materials").forGetter(TrimDataConfig::darkerMaterials),
        Identifier.CODEC.listOf().fieldOf("extra_patterns").forGetter(TrimDataConfig::extraPatterns),
        Identifier.CODEC.listOf().fieldOf("extra_materials").forGetter(TrimDataConfig::extraMaterials)
    ).apply(instance, TrimDataConfig::new));

    public static final TrimDataConfig DEFAULT = new TrimDataConfig(
        Map.of(
            id("axe"), ItemTags.AXES,
            id("hoe"), ItemTags.HOES,
            id("pickaxe"), ItemTags.PICKAXES,
            id("shovel"), ItemTags.SHOVELS,
            id("sword"), ItemTags.SWORDS
        ),
        Map.ofEntries(
            darkerMaterial("copper"),
            darkerMaterial("iron"),
            darkerMaterial("golden"),
            darkerMaterial("diamond"),
            darkerMaterial("netherite")
        ),
        List.of(),
        List.of()
    );

    private static Map.Entry<String, List<Identifier>> darkerMaterial(String prefix) {
        Identifier base = vanilla(prefix);
        if (prefix.equals("golden")) prefix = "gold";

        return Map.entry(prefix, List.of(
                base.withSuffix("_axe"),
                base.withSuffix("_hoe"),
                base.withSuffix("_pickaxe"),
                base.withSuffix("_shovel"),
                base.withSuffix("_sword")
        ));
    }

    private static Identifier vanilla(String name) {
        return Identifier.withDefaultNamespace(name);
    }

}
