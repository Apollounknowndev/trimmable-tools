package dev.worldgen.trimmable.tools.platform.services;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IPlatformHelper {
    Path getConfigFolder();
    List<Path> findPaths(String name);
    Map<String, Path> findFolders(String name);
}