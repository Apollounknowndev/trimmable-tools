package dev.worldgen.trimmable.tools.forge;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
}