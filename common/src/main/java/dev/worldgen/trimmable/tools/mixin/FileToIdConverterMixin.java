package dev.worldgen.trimmable.tools.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.worldgen.trimmable.tools.resource.TrimmableToolsResourceHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(FileToIdConverter.class)
public class FileToIdConverterMixin {
    @Shadow
    @Final
    private String prefix;

    @ModifyReturnValue(
        method = "listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;",
        at = @At("RETURN")
    )
    private Map<ResourceLocation, Resource> trimmableTools$addTrimTools(Map<ResourceLocation, Resource> map) {
        if (this.prefix.equals("models")) {
            Map<ResourceLocation, Resource> resources = new HashMap<>(map);
            TrimmableToolsResourceHelper.addAllTrimOverrides(resources);
            return resources;
        }
        return map;
    }
}
