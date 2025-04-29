package dev.worldgen.trimmable.tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class TrimmableTools {
    public static final String MOD_ID = "trimmable_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}