package com.simpleskins.simpleskins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps player UUID to the premium name whose skin they wear, plus the fetched
 * textures so joins never re-hit Mojang until the player changes skin.
 */
public final class SkinStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Entry>>() {
    }.getType();

    private final Path file;
    private final Map<String, Entry> entries = new HashMap<>();

    public SkinStore(Path file) {
        this.file = file;
    }

    public synchronized void load() throws IOException {
        entries.clear();
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Map<String, Entry> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                entries.putAll(loaded);
            }
        }
    }

    public synchronized void save() throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(entries, writer);
        }
    }

    public synchronized Entry get(UUID id) {
        return entries.get(id.toString());
    }

    public synchronized void put(UUID id, String skinName, String value, String signature) {
        entries.put(id.toString(), new Entry(skinName, value, signature));
    }

    public synchronized void remove(UUID id) {
        entries.remove(id.toString());
    }

    public synchronized Map<UUID, Entry> all() {
        Map<UUID, Entry> out = new HashMap<>();
        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            out.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        return out;
    }

    public static final class Entry {
        public String skinName;
        public String value;
        public String signature;

        public Entry(String skinName, String value, String signature) {
            this.skinName = skinName;
            this.value = value;
            this.signature = signature;
        }
    }
}
