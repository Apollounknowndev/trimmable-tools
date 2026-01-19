package dev.worldgen.trimmable.tools;

import net.msrandom.multiplatform.annotations.Expect;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TrimmableToolsHelper {
    @Expect
    public static Path getConfigFolder();

    @Expect
    public static List<Path> findPaths(String name);

    @Expect
    public static Map<String, Path> findFolders(String name);

    @Expect
    public static boolean isModLoaded(String modId);
}
