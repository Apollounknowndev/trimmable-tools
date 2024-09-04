package dev.worldgen.trimmable.tools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.server.loottable.vanilla.VanillaEquipmentLootTableGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.trim.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.ReferenceLootFunction;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrimmableTools implements ModInitializer {
	public static final String MOD_ID = "trimmable_tools";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final RegistryKey<LootFunction> TRIAL_CHAMBERS_EQUIPMENT = RegistryKey.of(RegistryKeys.ITEM_MODIFIER, id("trial_chambers_equipment"));

	@Override
	public void onInitialize() {
		LootTableEvents.MODIFY.register(((key, tableBuilder, source, registries) -> {
			if (LootTables.TRIAL_CHAMBER_MELEE_EQUIPMENT.getValue().equals(key.getValue())) {
				tableBuilder.apply(ReferenceLootFunction.builder(TRIAL_CHAMBERS_EQUIPMENT));
			}
		}));
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
}