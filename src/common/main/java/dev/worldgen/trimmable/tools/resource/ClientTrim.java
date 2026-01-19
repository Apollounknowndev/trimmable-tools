package dev.worldgen.trimmable.tools.resource;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

public record ClientTrim(Identifier material, Identifier pattern) {
    private static final Identifier UNKNOWN = Identifier.withDefaultNamespace("unknown");

    public static ClientTrim create(@Nullable ArmorTrim trim) {
        if (trim == null) return new ClientTrim(UNKNOWN, UNKNOWN);

        Identifier material = trim.material().unwrapKey().map(ResourceKey::identifier).orElse(UNKNOWN);
        Identifier pattern = trim.pattern().unwrapKey().map(ResourceKey::identifier).orElse(UNKNOWN);
        return new ClientTrim(material, pattern);
    }
}
