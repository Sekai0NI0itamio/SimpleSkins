package com.simpleskins.simpleskins;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Mojang lookups run off-thread. Pure parsing stays unit-testable.
 */
public final class MojangFetch {
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson GSON = new Gson();

    private MojangFetch() {
    }

    public static boolean validName(String name) {
        return name != null && NAME.matcher(name).matches();
    }

    public record Skin(String value, String signature) {
    }

    public static Skin fetch(String premiumName) {
        try {
            String id = profileId(premiumName);
            if (id == null) {
                return null;
            }
            return textures(id);
        } catch (IOException | InterruptedException e) {
            SimpleSkins.LOGGER.error("Mojang lookup failed for {}", premiumName, e);
            return null;
        }
    }

    private static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "SimpleSkins/1.0")
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 429) {
            SimpleSkins.LOGGER.error("Mojang rate limit hit, try again later");
            return null;
        }
        if (response.statusCode() != 200) {
            return null;
        }
        return response.body();
    }

    private static String profileId(String name) throws IOException, InterruptedException {
        String body = get("https://api.mojang.com/users/profiles/minecraft/" + name);
        return body == null ? null : parseProfileId(body);
    }

    private static Skin textures(String id) throws IOException, InterruptedException {
        String body = get("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false");
        return body == null ? null : parseTextures(body);
    }

    static String parseProfileId(String body) {
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        return json.has("id") ? json.get("id").getAsString() : null;
    }

    static Skin parseTextures(String body) {
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        JsonArray properties = json.getAsJsonArray("properties");
        if (properties == null) {
            return null;
        }
        for (int i = 0; i < properties.size(); i++) {
            JsonObject property = properties.get(i).getAsJsonObject();
            if ("textures".equals(property.get("name").getAsString())) {
                String value = property.get("value").getAsString();
                String signature = property.has("signature") ? property.get("signature").getAsString() : "";
                return new Skin(value, signature);
            }
        }
        return null;
    }

    public static boolean slimModel(String texturesValue) {
        try {
            String decoded = new String(Base64.getDecoder().decode(texturesValue));
            JsonObject json = JsonParser.parseString(decoded).getAsJsonObject();
            JsonObject skin = json.getAsJsonObject("textures").getAsJsonObject("SKIN");
            return skin != null && skin.has("metadata")
                    && "slim".equalsIgnoreCase(skin.getAsJsonObject("metadata").get("model").getAsString());
        } catch (RuntimeException e) {
            return false;
        }
    }
}
