package dev.worldgen.trimmable.tools;

import dev.worldgen.trimmable.tools.config.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.BufferedReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class TrimmableToolsClient implements ClientModInitializer {
	public static final Identifier TRIM_PATTERN = TrimmableTools.id("trim_pattern");
	public static final Identifier TRIM_MATERIAL = TrimmableTools.id("trim_material");

	@Override
	public void onInitializeClient() {
		ConfigHandler.load();

		ModelPredicateProviderRegistry.register(TRIM_PATTERN, (stack, world, entity, seed) -> {
			ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
			if (trim == null) return Float.NEGATIVE_INFINITY;

			Identifier id = trim.getPattern().getKey().orElse(ArmorTrimPatterns.COAST).getValue();
			return (ConfigHandler.patterns().indexOf(id) + 1f) / 1000;
		});

		ModelPredicateProviderRegistry.register(TRIM_MATERIAL, (stack, world, entity, seed) -> {
			ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
			if (trim == null) return Float.NEGATIVE_INFINITY;

			Identifier id = trim.getMaterial().getKey().orElse(ArmorTrimMaterials.REDSTONE).getValue();
			return (ConfigHandler.materials().indexOf(id) + 1f) / 1000;
		});
	}
}