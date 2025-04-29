package dev.worldgen.trimmable.tools.mixin;

import com.mojang.serialization.MapCodec;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.resource.TrimmedItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER;

    @Inject(
        method = "bootstrap",
        at = @At("RETURN")
    )
    private static void addTrimmedItemModel(CallbackInfo ci) {
        ID_MAPPER.put(TrimmableTools.id("trimmed"), TrimmedItemModel.Unbaked.MAP_CODEC);
    }
}
