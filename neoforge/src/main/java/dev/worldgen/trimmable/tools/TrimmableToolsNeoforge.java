package dev.worldgen.trimmable.tools;

import com.mojang.serialization.MapCodec;
import dev.worldgen.trimmable.tools.loot.ApplyItemModifierLootModifier;
import dev.worldgen.trimmable.tools.resource.TrimPalettedPermutations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(TrimmableTools.MOD_ID)
@SuppressWarnings("unused")
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
        }

        @SubscribeEvent
        public static void registerAtlasSource(RegisterSpriteSourcesEvent event) {
            event.register(TrimmableTools.id("paletted_permutations"), TrimPalettedPermutations.CODEC);
        }
    }
}