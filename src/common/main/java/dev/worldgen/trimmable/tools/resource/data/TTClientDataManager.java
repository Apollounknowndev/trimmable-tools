package dev.worldgen.trimmable.tools.resource.data;

import com.mojang.serialization.JsonOps;
import dev.worldgen.trimmable.tools.TrimmableTools;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;

public class TTClientDataManager extends SimplePreparableReloadListener<TTClientData> {
    public static final Identifier ID = TrimmableTools.id("trimmable_tools");
    public static final TTClientDataManager INSTANCE = new TTClientDataManager();

    private TTClientData clientData;

    private TTClientDataManager() {}

    @Override
    protected TTClientData prepare(ResourceManager manager, ProfilerFiller profiler) {
        TTClientData data = TTClientData.EMPTY;
        for (String namespace : manager.getNamespaces()) {
            var resource = manager.getResource(TrimmableTools.id(namespace, "trimmable_tools.json"));
            if (resource.isEmpty()) continue;

            try {
                BufferedReader reader = resource.get().openAsReader();
                var dataResult = TTClientData.CODEC.parse(JsonOps.INSTANCE, LenientJsonParser.parse(reader));
                if (dataResult.isSuccess()) {
                    data = TTClientData.merge(data, dataResult.getOrThrow());
                } else {
                    throw new IllegalStateException(dataResult.error().orElseThrow().message());
                }
            } catch (Exception e) {
                TrimmableTools.LOGGER.warn("Couldn't parse trimmable tools data from {} namespace: {}", namespace, e);
            }
        }
        this.clientData = data;
        return data;
    }

    @Override
    protected void apply(TTClientData preparations, ResourceManager manager, ProfilerFiller profiler) {

    }

    public TTClientData getClientData() {
        if (clientData == null) {
            TrimmableTools.LOGGER.error("Couldn't find client data, returning default");
            return TTClientData.EMPTY;
        }
        return clientData;
    }
}
