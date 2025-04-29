package dev.worldgen.trimmable.tools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.config.ToolTags;
import dev.worldgen.trimmable.tools.resource.TrimmableToolsResourceHelper;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientItemInfoLoader.LoadedClientInfos.class)
public class ClientItemInfoLoaderMixin {
    @Shadow
    @Final
    private Map<ResourceLocation, ClientItem> contents;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void injectModifiedItemModels(CallbackInfo ci) {
        Map<ResourceLocation, ClientItem> modifiedContents = new HashMap<>(this.contents);

        for (Map.Entry<ResourceLocation, ClientItem> info : modifiedContents.entrySet()) {
            ResourceLocation itemId = info.getKey();
            if (ToolTags.getToolType(itemId).equals(ToolTags.UNKNOWN)) continue;

            modifiedContents.put(
                itemId,
                new ClientItem(
                    TrimmableToolsResourceHelper.createItemModel(
                        itemId,
                        info.getValue().model()
                    ), ClientItem.Properties.DEFAULT
                )
            );
        }

        this.contents.clear();
        this.contents.putAll(modifiedContents);
    }
}
