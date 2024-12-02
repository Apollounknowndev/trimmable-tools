package dev.worldgen.trimmable.tools.platform;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public List<Path> findPaths(String name) {
        ArrayList<Path> paths = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            paths.add(mod.getOwningFile().getFile().findResource(name.split("/")));
        }
        return paths;
    }

    @Override
    public Map<String, Path> findFolders(String name) {
        Map<String, Path> paths = new HashMap<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modId = mod.getModId();
            paths.put(modId, mod.getOwningFile().getFile().findResource(name.formatted(modId).split("/")));
        }
        return paths;
    }
}