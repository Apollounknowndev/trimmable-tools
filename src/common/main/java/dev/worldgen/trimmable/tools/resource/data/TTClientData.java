package dev.worldgen.trimmable.tools.resource.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.trimmable.tools.TrimmableToolsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.*;

public record TTClientData(Map<Identifier, List<Identifier>> toolTypes, List<PatternData> patterns, List<MaterialData> materials) {
    public static final TTClientData EMPTY = new TTClientData(Map.of(), List.of(), List.of());
    private static final Codec<List<Identifier>> ID_LIST_CODEC = ExtraCodecs.compactListCodec(Identifier.CODEC);
    public static final Codec<TTClientData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(Identifier.CODEC, ID_LIST_CODEC).optionalFieldOf("tool_types", Map.of()).forGetter(TTClientData::toolTypes),
        PatternData.CODEC.listOf().optionalFieldOf("patterns", List.of()).forGetter(TTClientData::patterns),
        MaterialData.CODEC.listOf().optionalFieldOf("materials", List.of()).forGetter(TTClientData::materials)
    ).apply(i, TTClientData::new));

    public TTClientData(Map<Identifier, List<Identifier>> toolTypes, List<PatternData> patterns, List<MaterialData> materials) {
        this.toolTypes = toolTypes;
        this.patterns = patterns.stream().filter(data -> data.requiredMod().map(TrimmableToolsHelper::isModLoaded).orElse(true)).toList();
        this.materials = materials.stream().filter(data -> data.requiredMod().map(TrimmableToolsHelper::isModLoaded).orElse(true)).toList();
    }

    public Optional<Identifier> getToolType(Identifier id) {
        for (var entry : this.toolTypes().entrySet()) {
            if (entry.getValue().contains(id)) return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }

    public static TTClientData merge(TTClientData a, TTClientData b) {
        Map<Identifier, List<Identifier>> toolTypes = new HashMap<>();
        for (var entry : a.toolTypes().entrySet()) {
            toolTypes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (var entry : b.toolTypes().entrySet()) {
            if (toolTypes.containsKey(entry.getKey())) {
                toolTypes.get(entry.getKey()).addAll(entry.getValue());
            } else {
                toolTypes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        List<PatternData> patterns = new ArrayList<>(a.patterns());
        patterns.addAll(b.patterns());

        List<MaterialData> materials = new ArrayList<>(a.materials());
        materials.addAll(b.materials());

        return new TTClientData(toolTypes, patterns, materials);
    }

    public record PatternData(Identifier id, Optional<String> requiredMod) {
        private static final Codec<PatternData> SIMPLE_CODEC = Identifier.CODEC.xmap(id -> new PatternData(id, Optional.empty()), PatternData::id);
        private static final Codec<PatternData> EXPANDED_CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("id").forGetter(PatternData::id),
            Codec.STRING.optionalFieldOf("required_mod").forGetter(PatternData::requiredMod)
        ).apply(i, PatternData::new));
        public static final Codec<PatternData> CODEC = Codec.withAlternative(SIMPLE_CODEC, EXPANDED_CODEC);
    }

    public record MaterialData(Identifier id, Optional<Map<Identifier, List<Identifier>>> overrides, Optional<String> requiredMod) {
        private static final Codec<MaterialData> SIMPLE_CODEC = Identifier.CODEC.xmap(id -> new MaterialData(id, Optional.empty(), Optional.empty()), MaterialData::id);
        private static final Codec<MaterialData> EXPANDED_CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("id").forGetter(MaterialData::id),
            Codec.unboundedMap(Identifier.CODEC, ID_LIST_CODEC).optionalFieldOf("overrides").forGetter(MaterialData::overrides),
            Codec.STRING.optionalFieldOf("required_mod").forGetter(MaterialData::requiredMod)
        ).apply(i, MaterialData::new));
        public static final Codec<MaterialData> CODEC = Codec.withAlternative(SIMPLE_CODEC, EXPANDED_CODEC);

        public Identifier getMaterial(Identifier itemId) {
            if (this.overrides().isEmpty()) return this.id;
            for (var entry : this.overrides().get().entrySet()) {
                if (entry.getValue().contains(itemId)) return entry.getKey();
            }
            return this.id;
        }
    }
}
