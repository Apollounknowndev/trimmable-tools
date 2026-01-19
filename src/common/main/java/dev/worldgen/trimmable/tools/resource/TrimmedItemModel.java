package dev.worldgen.trimmable.tools.resource;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record TrimmedItemModel(Object2ObjectMap<ClientTrim, ItemModel> models) implements ItemModel {
    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner holder, int i) {
        ClientTrim trim = ClientTrim.create(stack.get(DataComponents.TRIM));
        ItemModel model = this.models.get(trim);
        if (model != null) {
            model.update(renderState, stack, resolver, context, level, holder, i);
        }
    }

    public record Unbaked(Object2ObjectMap<ClientTrim, ItemModel.Unbaked> cases, Identifier itemName, ItemModel.Unbaked fallback) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("item_name").forGetter(Unbaked::itemName),
            ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(instance, TrimmableToolsResourceHelper::createItemModel));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public ItemModel bake(BakingContext context) {
            Object2ObjectMap<ClientTrim, ItemModel> models = new Object2ObjectOpenHashMap<>();
            for (var entry : this.cases.entrySet()) {
                try {
                    var model = entry.getValue().bake(context);
                    models.put(entry.getKey(), model);
                } catch (Exception e) {

                }
            }
            models.defaultReturnValue(this.fallback.bake(context));

            return new TrimmedItemModel(models);
        }

        public void resolveDependencies(Resolver resolver) {
            this.cases.forEach((key, value) -> value.resolveDependencies(resolver));
            this.fallback.resolveDependencies(resolver);
        }
    }
}
