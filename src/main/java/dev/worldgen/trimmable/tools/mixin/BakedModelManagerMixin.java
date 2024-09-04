package dev.worldgen.trimmable.tools.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.worldgen.trimmable.tools.resource.TrimmableToolsResourceHelper;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {

    @ModifyReturnValue(
        method = "method_45895",
        at = @At("RETURN")
    )
    private static Map<Identifier, Resource> trimmableTools$addTrimOverrides(Map<Identifier, Resource> map) {
        Map<Identifier, Resource> resources = new HashMap<>(map);
        TrimmableToolsResourceHelper.addAllTrimOverrides(resources);
        return resources;
    }
}
