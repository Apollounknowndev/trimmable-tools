package dev.worldgen.trimmable.tools;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrimmableTools {
    public static final String MOD_ID = "trimmable_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void removeLegacyConfig() {
        if (TrimmableToolsHelper.getConfigFolder().resolve("trimmable_tools.json").toFile().delete()) {
            LOGGER.info("Deleted legacy trimmable tools config file");
        }
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}