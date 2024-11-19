package dev.worldgen.trimmable.tools;


import com.mojang.serialization.MapCodec;
import dev.worldgen.trimmable.tools.config.ConfigHandler;
import dev.worldgen.trimmable.tools.loot.ApplyItemModifierLootModifier;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(TrimmableTools.MOD_ID)
public class TrimmableToolsNeoforge {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TrimmableTools.MOD_ID);
    public static final Supplier<MapCodec<ApplyItemModifierLootModifier>> APPLY_ITEM_MODIFIER = LOOT_MODIFIERS.register("apply_item_modifier", () -> ApplyItemModifierLootModifier.CODEC);

    public TrimmableToolsNeoforge(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = TrimmableTools.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TrimmableToolsClient.init();

            ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_PATTERN, (stack, world, entity, seed) -> {
                if (world == null) return Float.NEGATIVE_INFINITY;
                ArmorTrim trim = stack.get(DataComponents.TRIM);
                if (trim == null) return Float.NEGATIVE_INFINITY;

                ResourceLocation id = trim.pattern().unwrapKey().orElse(TrimPatterns.COAST).location();
                return (ConfigHandler.patterns().indexOf(id) + 1f) / 1000;
            });

            ItemProperties.registerGeneric(TrimmableToolsClient.TRIM_MATERIAL, (stack, world, entity, seed) -> {
                if (world == null) return Float.NEGATIVE_INFINITY;
                ArmorTrim trim = stack.get(DataComponents.TRIM);
                if (trim == null) return Float.NEGATIVE_INFINITY;

                ResourceLocation id = trim.material().unwrapKey().orElse(TrimMaterials.REDSTONE).location();
                return (ConfigHandler.materials().indexOf(id) + 1f) / 1000;
            });
        }
    }
}