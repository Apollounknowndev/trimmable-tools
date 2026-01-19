package dev.worldgen.trimmable.tools;

import net.msrandom.multiplatform.annotations.Actual;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimmableToolsHelperActual {
    @Actual
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Actual
    public List<Path> findPaths(String name) {
        ArrayList<Path> paths = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            paths.add(mod.getOwningFile().getFile().findResource(name.split("/")));
        }
        return paths;
    }

    @Actual
    public Map<String, Path> findFolders(String name) {
        Map<String, Path> paths = new HashMap<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modId = mod.getModId();
            paths.put(modId, mod.getOwningFile().getFile().findResource(name.formatted(modId).split("/")));
        }
        return paths;
    }
}