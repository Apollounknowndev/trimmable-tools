package dev.worldgen.trimmable.tools.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.worldgen.trimmable.tools.TrimmableTools;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(ItemModelGenerator.class)
public abstract class ItemModelGeneratorMixin {

    @Inject(
        method = "createSideElements",
        at = @At("HEAD")
    )
    private void trimmableTools$saveLayer(SpriteContents sprite, String key, int layer, CallbackInfoReturnable<List<BlockElement>> cir, @Share("expandElements") LocalBooleanRef expandElements) {
        expandElements.set(sprite.name().getNamespace().equals(TrimmableTools.MOD_ID));
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 0
        ), index = 0
    )
    private Vector3f trimmableTools$fixUp1(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(0, 0.01f, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 0
        ), index = 1
    )
    private Vector3f trimmableTools$fixUp2(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(0, 0.01f, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 1
        ), index = 0
    )
    private Vector3f trimmableTools$fixDown1(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(0, -0.01f, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 1
        ), index = 1
    )
    private Vector3f trimmableTools$fixDown2(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(0, -0.01f, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 2
        ), index = 0
    )
    private Vector3f trimmableTools$fixRight1(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(-0.01f, 0, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 2
        ), index = 1
    )
    private Vector3f trimmableTools$fixRight2(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(-0.01f, 0, 0) : vec3f;
    }

    @ModifyArg(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 3
        ), index = 0
    )
    private Vector3f trimmableTools$fixLeft1(Vector3f vec3f, @Share("expandElements") LocalBooleanRef expandElements) {
        return expandElements.get() ? vec3f.add(0.01f, 0, 0) : vec3f;
    }
}
