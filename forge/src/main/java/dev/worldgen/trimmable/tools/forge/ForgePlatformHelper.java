package dev.worldgen.trimmable.tools.forge;

import dev.worldgen.trimmable.tools.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgePlatformHelper implements IPlatformHelper {
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