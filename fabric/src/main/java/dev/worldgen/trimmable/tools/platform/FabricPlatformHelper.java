package dev.worldgen.trimmable.tools.platform;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
