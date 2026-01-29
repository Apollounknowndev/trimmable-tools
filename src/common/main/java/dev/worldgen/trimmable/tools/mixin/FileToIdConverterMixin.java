package dev.worldgen.trimmable.tools.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.worldgen.trimmable.tools.resource.TTModelHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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
    private Map<Identifier, Resource> trimmableTools$addTrimTools(Map<Identifier, Resource> map, ResourceManager manager) {
        if (this.prefix.equals("models")) {
            Map<Identifier, Resource> resources = new HashMap<>(map);
            TTModelHelper.addAllTrimOverrides(manager, resources);
            return resources;
        }
        return map;
    }
}
