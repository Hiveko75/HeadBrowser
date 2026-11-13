package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class MinecraftHeadsAPI {

    public static final File HEADS_FILE = new File(CLIENT.runDirectory, "headbrowser_cache.json");
    private static final Gson GSON = new GsonBuilder().create();
    // The Minecraft Heads API used by Head Browser 2.6 (the old /scripts/api.php no longer answers).
    private static final String MINECRAFT_HEADS_API = "https://minecraft-heads.com/api/heads/";
    private static final String CUSTOM_HEADS_ENDPOINT = "custom-heads";
    private static final String CATEGORIES_ENDPOINT = "categories";
    private static final String APP_UUID = "ecdf3625-9a93-4481-b8be-a32f25ca1ea0";

    public static volatile HEADS HEADS = new HEADS();
    private static volatile boolean downloading = false;

    private static boolean downloadDatabase() {

        LOGGER.info("Starting Head database download...");
        long downloadStart = System.currentTimeMillis();

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        List<Head> heads = new ArrayList<>();
        Map<Integer, CATEGORY> categories = new HashMap<>();

        try {
            JsonObject categoryResponse = request(client, CATEGORIES_ENDPOINT, "");
            for (JsonElement entry : categoryResponse.getAsJsonArray("data")) {
                JsonObject category = entry.getAsJsonObject();
                categories.put(category.get("id").getAsInt(), CATEGORY.fromApiName(category.get("n").getAsString()));
            }

            int pages = 1;
            for (int page = 1; page <= pages; page++) {
                JsonObject response = request(client, CUSTOM_HEADS_ENDPOINT, "&page=" + page);
                pages = response.getAsJsonObject("pagination").get("last_page").getAsInt();

                for (JsonElement entry : response.getAsJsonArray("data")) {
                    JsonObject head = entry.getAsJsonObject();
                    String texture = head.get("u").getAsString();
                    heads.add(new Head(
                            head.get("n").getAsString(),
                            encodeTextureToValue(texture),
                            new String[0],
                            categories.getOrDefault(head.get("c").getAsInt(), CATEGORY.MISCELLANEOUS),
                            UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8))
                    ));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to download head database. You are likely not connected to the internet.", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (RuntimeException e) {
            LOGGER.error("Minecraft Heads returned an unexpected data structure.", e);
            return false;
        }

        HEADS newHeads = new HEADS();
        newHeads.heads = heads;
        HEADS = newHeads;

        LOGGER.info("Finished Head database download in {}ms ({} heads)", System.currentTimeMillis() - downloadStart, heads.size());
        return true;
    }

    private static JsonObject request(HttpClient client, String endpoint, String query) throws IOException, InterruptedException {
        URI uri = URI.create(MINECRAFT_HEADS_API + endpoint + "?app_uuid=" + URLEncoder.encode(APP_UUID, StandardCharsets.UTF_8) + query);
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Minecraft Heads API returned HTTP " + response.statusCode() + " for " + endpoint);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static String encodeTextureToValue(String texture) {
        String json = format("{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/%s\"}}}", texture);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static void saveHeads() {
        LOGGER.info("Saving heads cache at {}", HEADS_FILE.getPath());
        try (FileWriter writer = new FileWriter(HEADS_FILE)) {
            writer.write(GSON.toJson(HEADS));
            LOGGER.info("Saved heads cache successfully!");
        } catch (IOException e) {
            LOGGER.error("Error saving heads cache", e);
        }
    }

    public static boolean readHeads() {
        LOGGER.info("Reading heads cache at {}", HEADS_FILE.getPath());
        try {
            HEADS = GSON.fromJson(Files.readString(HEADS_FILE.toPath()), MinecraftHeadsAPI.HEADS.class);
            LOGGER.info("Read heads cache successfully!");
            return true;
        } catch (NoSuchFileException e) {
            LOGGER.info("No heads cache was found.");
            return false;
        } catch (IOException e) {
            String errorMessage = "Error reading heads cache";
            presentError(errorMessage, e.toString());
            throw new RuntimeException(errorMessage, e);
        }
    }

    /** Downloads the head database on a background thread, so the game does not wait for it. */
    public static void downloadAndSaveHeads() {
        if (downloading) return;
        downloading = true;
        Thread thread = new Thread(() -> {
            try {
                if (downloadDatabase()) saveHeads();
            } finally {
                downloading = false;
            }
        }, "HeadBrowser-Download");
        thread.setDaemon(true);
        thread.start();
    }

    public record Head(String name, String value, String[] tags, CATEGORY category, UUID uuid) {

        public ItemStack toItem() {
                ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
                GameProfile profile = new GameProfile(this.uuid, "TheVoidBlock");
                profile.getProperties().put("textures", new Property("textures", this.value));
                head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
                head.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.name()).setStyle(Style.EMPTY.withItalic(false)));
                return head;
            }
        }

    public enum CATEGORY implements StringIdentifiable {
        ALPHABET("Alphabet"),
        ANIMALS("Animals"),
        BLOCKS("Blocks"),
        DECORATION("Decoration"),
        FOOD_DRINKS("Food-Drinks"),
        HUMANS("Humans"),
        HUMANOID("Humanoid"),
        MISCELLANEOUS("Miscellaneous"),
        MONSTERS("Monsters"),
        PLANTS("Plants");

        private final String string;

        CATEGORY(String string) {
            this.string = string;
        }

        /** Maps a category name of the Minecraft Heads API to the categories of this version. */
        public static CATEGORY fromApiName(String name) {
            String n = name.toLowerCase(Locale.ROOT);
            if (n.contains("alphabet") || n.contains("letter")) return ALPHABET;
            if (n.contains("animal")) return ANIMALS;
            if (n.contains("block")) return BLOCKS;
            if (n.contains("decor")) return DECORATION;
            if (n.contains("food") || n.contains("drink")) return FOOD_DRINKS;
            if (n.contains("humanoid")) return HUMANOID;
            if (n.contains("human")) return HUMANS;
            if (n.contains("monster")) return MONSTERS;
            if (n.contains("plant")) return PLANTS;
            return MISCELLANEOUS;
        }

        @Override
        public String asString() {
            return string;
        }
    }

    public static class HEADS {
        public List<Head> heads = new ArrayList<>();
        public long downloadTime = System.currentTimeMillis();

        public ItemStack getRandomHead() {
            Random random = new Random();

            if(!heads.isEmpty()) return heads.get(random.nextInt(0, heads.size())).toItem();
            else return Items.PLAYER_HEAD.getDefaultStack();
        }
    }
}
