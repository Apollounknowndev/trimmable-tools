package dev.worldgen.trimmable.tools.platform;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public List<Path> findPaths(String name) {
        ArrayList<Path> paths = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.findPath(name).ifPresent(paths::add);
        }
        return paths;
    }

    @Override
    public Map<String, Path> findFolders(String name) {
        Map<String, Path> paths = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            mod.findPath(name.formatted(modId)).ifPresent(path -> paths.put(modId, path));
        }
        return paths;
    }
}
