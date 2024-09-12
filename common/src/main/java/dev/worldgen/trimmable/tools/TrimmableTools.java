package dev.worldgen.trimmable.tools;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class TrimmableTools {
    public static final String MOD_ID = "trimmable_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}