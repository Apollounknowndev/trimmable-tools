package dev.worldgen.trimmable.tools.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class ApplyItemModifierLootModifier extends LootModifier {
    public static final MapCodec<ApplyItemModifierLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
        LootModifier.codecStart(instance).and(LootItemFunctions.CODEC.fieldOf("item_modifier").forGetter(ApplyItemModifierLootModifier::itemModifier)).apply(instance, ApplyItemModifierLootModifier::new)
    );

    private final Holder<LootItemFunction> itemModifier;

    protected ApplyItemModifierLootModifier(LootItemCondition[] conditionsIn, Holder<LootItemFunction> itemModifier) {
        super(conditionsIn);
        this.itemModifier = itemModifier;
    }

    public Holder<LootItemFunction> itemModifier() {
        return this.itemModifier;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (ItemStack stack : generatedLoot) {
            this.itemModifier.value().apply(stack, context);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
