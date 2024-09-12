package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;

import java.util.Optional;

public class TrimmableToolsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TrimmableToolsClient.init();

        ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_PATTERN, (stack, world, entity, seed) -> {
            Optional<ArmorTrim> trim = ArmorTrim.getTrim(world.registryAccess(), stack);
            if (trim.isEmpty()) return Float.NEGATIVE_INFINITY;

            ResourceLocation id = trim.get().pattern().unwrapKey().orElse(TrimPatterns.COAST).location();
            return (ConfigHandler.patterns().indexOf(id) + 1f) / 1000;
        });

        ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_MATERIAL, (stack, world, entity, seed) -> {
            Optional<ArmorTrim> trim = ArmorTrim.getTrim(world.registryAccess(), stack);
            if (trim.isEmpty()) return Float.NEGATIVE_INFINITY;

            ResourceLocation id = trim.get().material().unwrapKey().orElse(TrimMaterials.REDSTONE).location();
            return (ConfigHandler.materials().indexOf(id) + 1f) / 1000;
        });
    }
}
