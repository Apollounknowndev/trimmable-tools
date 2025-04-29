package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.mixin.SpriteSourcesAccessor;
import dev.worldgen.trimmable.tools.resource.TrimPalettedPermutations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
        SpriteSourcesAccessor.getIdMapper().put(TrimmableTools.id("paletted_permutations"), TrimPalettedPermutations.CODEC);
    }
}
