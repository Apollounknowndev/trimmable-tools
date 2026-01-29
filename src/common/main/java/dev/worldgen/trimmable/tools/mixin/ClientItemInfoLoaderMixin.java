package dev.worldgen.trimmable.tools.mixin;

import dev.worldgen.trimmable.tools.resource.TTModelHelper;
import dev.worldgen.trimmable.tools.resource.data.TTClientData;
import dev.worldgen.trimmable.tools.resource.data.TTClientDataManager;
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
import java.util.Optional;

@Mixin(ClientItemInfoLoader.LoadedClientInfos.class)
public class ClientItemInfoLoaderMixin {
    @Shadow
    @Final
    private Map<Identifier, ClientItem> contents;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void modifyClientItems(CallbackInfo ci) {
        Map<Identifier, ClientItem> modifiedContents = new HashMap<>(this.contents);
        TTClientData data = TTClientDataManager.INSTANCE.getClientData();

        for (Map.Entry<Identifier, ClientItem> info : modifiedContents.entrySet()) {
            Identifier itemId = info.getKey();
            Optional<Identifier> toolType = data.getToolType(itemId);
            if (toolType.isEmpty()) continue;

            modifiedContents.put(
                itemId,
                new ClientItem(
                    TTModelHelper.createItemModel(
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
