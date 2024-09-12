package dev.worldgen.trimmable.tools.platform;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
}