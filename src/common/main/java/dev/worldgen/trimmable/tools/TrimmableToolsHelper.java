package dev.worldgen.trimmable.tools;

import net.minecraft.resources.Identifier;
import net.msrandom.multiplatform.annotations.Expect;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class TrimmableToolsHelper {
    public static Identifier getTrimPermutationId(Identifier toolType, Identifier pattern, @Nullable Identifier material) {
        String path = "trims/items/" + toolType.getPath() + "/" + pattern.getPath();
        if (material != null) path = path + "_" + material.getPath();
        return Identifier.fromNamespaceAndPath(toolType.getNamespace(), path);
    }
    
    
    @Expect
    public static Path getConfigFolder();

    @Expect
    public static boolean isModLoaded(String modId);
}
