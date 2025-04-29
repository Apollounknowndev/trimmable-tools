package dev.worldgen.trimmable.tools.resource;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

public record ClientTrim(ResourceLocation material, ResourceLocation pattern) {
    private static final ResourceLocation UNKNOWN = ResourceLocation.withDefaultNamespace("unknown");

    public static ClientTrim create(@Nullable ArmorTrim trim) {
        if (trim == null) return new ClientTrim(UNKNOWN, UNKNOWN);

        ResourceLocation material = trim.material().unwrapKey().map(ResourceKey::location).orElse(UNKNOWN);
        ResourceLocation pattern = trim.pattern().unwrapKey().map(ResourceKey::location).orElse(UNKNOWN);
        return new ClientTrim(material, pattern);
    }
}
