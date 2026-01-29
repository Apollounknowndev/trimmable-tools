package dev.worldgen.trimmable.tools.fabric;

import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.mixin.SpriteSourcesAccessor;
import dev.worldgen.trimmable.tools.resource.TTPalettedPermutations;
import dev.worldgen.trimmable.tools.resource.data.TTClientDataManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.functions.FunctionReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class TrimmableToolsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrimmableTools.removeLegacyConfig();
        SpriteSourcesAccessor.getIdMapper().put(TrimmableTools.id("paletted_permutations"), TTPalettedPermutations.MAP_CODEC);

        ResourceLoader loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        loader.registerReloader(TTClientDataManager.ID, TTClientDataManager.INSTANCE);
        loader.addReloaderOrdering(TTClientDataManager.ID, ResourceReloaderKeys.Client.ATLAS);
    }
}
