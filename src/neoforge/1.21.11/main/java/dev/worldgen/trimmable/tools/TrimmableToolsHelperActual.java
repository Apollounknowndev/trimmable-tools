package dev.worldgen.trimmable.tools;

import net.msrandom.multiplatform.annotations.Actual;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class TrimmableToolsHelperActual {
    @Actual
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
    
    @Actual
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}