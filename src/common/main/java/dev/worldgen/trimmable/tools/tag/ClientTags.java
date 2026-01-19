/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.worldgen.trimmable.tools.tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.worldgen.trimmable.tools.TrimmableTools;
import dev.worldgen.trimmable.tools.TrimmableToolsHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;


// Stripped-down version of Fabric's client tag system.
public final class ClientTags {
    private static final Map<TagKey<?>, LoadedTag> LOCAL_TAG_HIERARCHY = new ConcurrentHashMap<>();

    public static Set<Identifier> getOrCreateLocalTag(TagKey<?> tagKey) {
        return ClientTags.getOrCreatePartiallySyncedTag(tagKey).completeIds();
    }

    public static LoadedTag getOrCreatePartiallySyncedTag(TagKey<?> tagKey) {
        LoadedTag loadedTag = LOCAL_TAG_HIERARCHY.get(tagKey);

        if (loadedTag == null) {
            loadedTag = ClientTags.loadTag(tagKey);
            LOCAL_TAG_HIERARCHY.put(tagKey, loadedTag);
        }

        return loadedTag;
    }

    public static LoadedTag loadTag(TagKey<?> tagKey) {
        var tags = new HashSet<TagEntry>();
        HashSet<Path> tagFiles = getDataFiles(tagKey.registry(), tagKey.location());

        for (Path tagPath : tagFiles) {
            try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
                JsonElement jsonElement = JsonParser.parseReader(tagReader);
                TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
                        .result().orElse(null);

                if (maybeTagFile != null) {
                    if (maybeTagFile.replace()) {
                        tags.clear();
                    }

                    tags.addAll(maybeTagFile.entries());
                }
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                TrimmableTools.LOGGER.error("Error loading tag: " + tagKey, e);
            }
        }

        HashSet<Identifier> completeIds = new HashSet<>();
        HashSet<Identifier> immediateChildIds = new HashSet<>();
        HashSet<TagKey<?>> immediateChildTags = new HashSet<>();

        for (TagEntry tagEntry : tags) {
            tagEntry.build(new TagEntry.Lookup<>() {
                @Override
                public Identifier element(Identifier id, boolean required) {
                    immediateChildIds.add(id);
                    return id;
                }

                @Override
                public Collection<Identifier> tag(Identifier id) {
                    TagKey<?> tag = TagKey.create(tagKey.registry(), id);
                    immediateChildTags.add(tag);
                    return ClientTags.getOrCreatePartiallySyncedTag(tag).completeIds;
                }
            }, completeIds::add);
        }

        immediateChildTags.remove(tagKey);

        return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags),
                Collections.unmodifiableSet(immediateChildIds));
    }

    public record LoadedTag(Set<Identifier> completeIds, Set<TagKey<?>> immediateChildTags, Set<Identifier> immediateChildIds) {
    }

    private static HashSet<Path> getDataFiles(ResourceKey<? extends Registry<?>> registryKey, Identifier identifier) {
        return getDataFiles(Registries.tagsDirPath(registryKey), identifier);
    }

    private static HashSet<Path> getDataFiles(String folder, Identifier identifier) {
        String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), folder, identifier.getPath());
        return new HashSet<>(TrimmableToolsHelper.findPaths(tagFile));
    }
}