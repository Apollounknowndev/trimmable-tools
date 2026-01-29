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
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
