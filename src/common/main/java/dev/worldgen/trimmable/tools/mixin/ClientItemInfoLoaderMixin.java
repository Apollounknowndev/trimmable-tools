package dev.worldgen.trimmable.tools.mixin;

import dev.worldgen.trimmable.tools.config.ToolTags;
import dev.worldgen.trimmable.tools.resource.TrimmableToolsResourceHelper;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
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
    private Map<Identifier, ClientItem> contents;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void injectModifiedItemModels(CallbackInfo ci) {
        Map<Identifier, ClientItem> modifiedContents = new HashMap<>(this.contents);

        for (Map.Entry<Identifier, ClientItem> info : modifiedContents.entrySet()) {
            Identifier itemId = info.getKey();
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
