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


    @ModifyArgs(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 0
        )
    )
    private void trimmableTools$fixUp(Args args, @Share("expandElements") LocalBooleanRef expandElements) {
        if (!expandElements.get()) return;

        Vector3f from = args.get(0);
        Vector3f to = args.get(1);

        from.add(0, 0.01F, 0);
        to.add(0, 0.01F, 0);

        args.set(0, from);
        args.set(1, to);
    }

    @ModifyArgs(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 1
        )
    )
    private void trimmableTools$fixDown(Args args, @Share("expandElements") LocalBooleanRef expandElements) {
        if (!expandElements.get()) return;

        Vector3f from = args.get(0);
        Vector3f to = args.get(1);

        from.add(0, -0.01F, 0);
        to.add(0, -0.01F, 0);

        args.set(0, from);
        args.set(1, to);
    }

    @ModifyArgs(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 2
        )
    )
    private void trimmableTools$fixRight(Args args, @Share("expandElements") LocalBooleanRef expandElements) {
        if (!expandElements.get()) return;

        Vector3f from = args.get(0);
        Vector3f to = args.get(1);

        from.add(-0.01F, 0, 0);
        to.add(-0.01F, 0, 0);

        args.set(0, from);
        args.set(1, to);
    }

    @ModifyArgs(
        method = "createSideElements",
        at = @At(
            value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/block/model/BlockElement;<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
            ordinal = 3
        )
    )
    private void trimmableTools$fixLeft(Args args, @Share("expandElements") LocalBooleanRef expandElements) {
        if (!expandElements.get()) return;

        Vector3f from = args.get(0);
        Vector3f to = args.get(1);

        from.add(0.01F, 0, 0);
        to.add(0.01F, 0, 0);

        args.set(0, from);
        args.set(1, to);
    }
}
