package dev.worldgen.trimmable.tools;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class TrimmableTools {
    public static final String MOD_ID = "trimmable_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}