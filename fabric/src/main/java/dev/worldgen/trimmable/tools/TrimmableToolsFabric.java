package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.functions.FunctionReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class TrimmableToolsFabric implements ModInitializer, ClientModInitializer {
    private static final ResourceKey<LootItemFunction> TRIAL_CHAMBERS_EQUIPMENT = ResourceKey.create(Registries.ITEM_MODIFIER, TrimmableTools.id("trial_chambers_equipment"));

    @Override
    public void onInitialize() {
        LootTableEvents.MODIFY.register(((key, tableBuilder, source, registries) -> {
            if (BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE.location().equals(key.location())) {
                tableBuilder.apply(FunctionReference.functionReference(TRIAL_CHAMBERS_EQUIPMENT));
            }
        }));
    }

    @Override
    public void onInitializeClient() {
        TrimmableToolsClient.init();

        ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_PATTERN, (stack, world, entity, seed) -> {
            ArmorTrim trim = stack.get(DataComponents.TRIM);
            if (trim == null) return Float.NEGATIVE_INFINITY;

            ResourceLocation id = trim.pattern().unwrapKey().orElse(TrimPatterns.COAST).location();
            return (ConfigHandler.patterns().indexOf(id) + 1f) / 1000;
        });

        ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_MATERIAL, (stack, world, entity, seed) -> {
            ArmorTrim trim = stack.get(DataComponents.TRIM);
            if (trim == null) return Float.NEGATIVE_INFINITY;

            ResourceLocation id = trim.material().unwrapKey().orElse(TrimMaterials.REDSTONE).location();
            return (ConfigHandler.materials().indexOf(id) + 1f) / 1000;
        });
    }
}
