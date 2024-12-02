package dev.worldgen.trimmable.tools.forge;


import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsClient;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import dev.worldgen.trimmable.tools.config.TrimData;
import dev.worldgen.trimmable.tools.mixin.SpriteSourcesAccessor;
import dev.worldgen.trimmable.tools.resource.TrimPalettedPermutations;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterTextureAtlasSpriteLoadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Optional;

@Mod(TrimmableTools.MOD_ID)
@SuppressWarnings("unused")
public class TrimmableToolsForge {

    public TrimmableToolsForge() {
    }

    @Mod.EventBusSubscriber(modid = TrimmableTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TrimmableToolsClient.init();

            ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_PATTERN, (stack, world, entity, seed) -> {
                if (world == null) return Float.NEGATIVE_INFINITY;
                Optional<ArmorTrim> trim = ArmorTrim.getTrim(world.registryAccess(), stack);
                if (trim.isEmpty()) return Float.NEGATIVE_INFINITY;

                ResourceLocation id = trim.get().pattern().unwrapKey().orElse(TrimPatterns.COAST).location();
                return (TrimData.PATTERNS.indexOf(id) + 1f) / 1000;
            });

            ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_MATERIAL, (stack, world, entity, seed) -> {
                if (world == null) return Float.NEGATIVE_INFINITY;
                Optional<ArmorTrim> trim = ArmorTrim.getTrim(world.registryAccess(), stack);
                if (trim.isEmpty()) return Float.NEGATIVE_INFINITY;

                ResourceLocation id = trim.get().material().unwrapKey().orElse(TrimMaterials.REDSTONE).location();
                return (TrimData.MATERIALS.indexOf(id) + 1f) / 1000;
            });
        }
    }
}
