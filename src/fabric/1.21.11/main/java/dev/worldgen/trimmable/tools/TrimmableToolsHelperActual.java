package dev.worldgen.trimmable.tools;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.msrandom.multiplatform.annotations.Actual;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimmableToolsHelperActual {
    @Actual
    public static Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Actual
    public static List<Path> findPaths(String name) {
        ArrayList<Path> paths = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.findPath(name).ifPresent(paths::add);
        }
        return paths;
    }

    @Actual
    public static Map<String, Path> findFolders(String name) {
        Map<String, Path> paths = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            mod.findPath(name.formatted(modId)).ifPresent(path -> paths.put(modId, path));
        }
        return paths;
    }

    @Actual
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
