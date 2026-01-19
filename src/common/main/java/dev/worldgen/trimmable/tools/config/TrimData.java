package dev.worldgen.trimmable.tools.config;

import dev.worldgen.trimmable.tools.TrimmableToolsHelper;
import net.minecraft.resources.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TrimData {
    public static final List<Identifier> MATERIALS = getTrimIds("trim_material");
    public static final List<Identifier> PATTERNS = getTrimIds("trim_pattern");

    public static List<Identifier> getTrimIds(String folder) {
        String tagFile = "data/%s/"+folder+"/";
        Map<String, Path> paths = TrimmableToolsHelper.findFolders(tagFile);

        List<Identifier> ids = new ArrayList<>();
        switch (folder) {
            case "trim_material": ids.addAll(ConfigHandler.config().extraMaterials());
            case "trim_pattern": ids.addAll(ConfigHandler.config().extraPatterns());
            default: break;
        }

        for (Map.Entry<String, Path> entry : paths.entrySet()) {
            Path folderPath = entry.getValue();

            try (Stream<Path> files = Files.list(folderPath)) {
                files.forEach(file -> {
                    String name = file.toString();
                    if (name.endsWith(".json")) {
                        List<String> split = Arrays.stream(name.split("/")).toList();
                        ids.add(Identifier.fromNamespaceAndPath(entry.getKey(), split.getLast().substring(0, split.getLast().length() - 5)));
                    }
                });
            } catch (Exception ignored) {}
        }

        return ids;
    }

    public static List<String> materials() {
        return MATERIALS.stream().map(Identifier::getPath).toList();
    }

    public static List<String> patterns() {
        return PATTERNS.stream().map(Identifier::getPath).toList();
    }
}
