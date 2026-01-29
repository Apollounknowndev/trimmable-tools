package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.resource.TTPalettedPermutations;
import dev.worldgen.trimmable.tools.resource.data.TTClientDataManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;

@Mod(TrimmableTools.MOD_ID)
@SuppressWarnings("unused")
public class TrimmableToolsNeoforge {
    public TrimmableToolsNeoforge(IEventBus eventBus) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = TrimmableTools.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TrimmableTools.removeLegacyConfig();
        }

        @SubscribeEvent
        public static void registerAtlasSource(RegisterSpriteSourcesEvent event) {
            event.register(TrimmableTools.id("paletted_permutations"), TTPalettedPermutations.MAP_CODEC);
        }
        
        @SubscribeEvent
        public static void registerReloaderOrder(AddClientReloadListenersEvent event) {
            event.addListener(TTClientDataManager.ID, TTClientDataManager.INSTANCE);
            event.addDependency(TTClientDataManager.ID, VanillaClientListeners.ATLASES);
        }
    }
}