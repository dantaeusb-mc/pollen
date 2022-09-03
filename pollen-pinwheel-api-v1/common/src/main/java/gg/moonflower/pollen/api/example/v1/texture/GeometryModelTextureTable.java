package gg.moonflower.pollen.api.example.v1.texture;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import gg.moonflower.pollen.pinwheel.api.client.geometry.GeometryModel;
import gg.moonflower.pollen.pinwheel.api.client.texture.GeometryTextureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

/**
 * <p>A table of textures to be used for {@link GeometryModel} rendering. Texture tables must be made from {@link GeometryTextureManager}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelTextureTable {

    public static final Codec<GeometryModelTextureTable> CODEC = Codec.unboundedMap(Codec.STRING,
                    Codec.either(
                            GeometryModelTexture.CODEC.listOf().xmap(list -> list.toArray(new GeometryModelTexture[0]), Arrays::asList),
                            GeometryModelTexture.CODEC.xmap(texture -> new GeometryModelTexture[]{texture}, array -> array.length > 0 ? array[0] : GeometryModelTexture.MISSING)
                    ).xmap(either -> either.left().orElseGet(() -> either.right().orElseThrow(() -> new NoSuchElementException("No value present"))), array -> array.length > 1 ? Either.left(array) : Either.right(array))) // Left is multiple layers, right is one layer
            .xmap(GeometryModelTextureTable::new, table -> table.textures);
    public static GeometryModelTextureTable EMPTY = new GeometryModelTextureTable(new HashMap<>());
    private static final GeometryModelTexture[] MISSING = new GeometryModelTexture[]{GeometryModelTexture.MISSING};

    private final Map<String, GeometryModelTexture[]> textures;

    public GeometryModelTextureTable(Map<String, GeometryModelTexture[]> textures) {
        this.textures = new HashMap<>(textures);
        this.textures.values().removeIf(layers -> layers.length == 0);
    }

    /**
     * Fetches a geometry model texture by the specified key.
     *
     * @param key The key of the textures to get
     * @return The texture with that key or {@link GeometryModelTexture#MISSING} if there is no texture bound to that key
     */
    public GeometryModelTexture[] getLayerTextures(@Nullable String key) {
        return this.textures.getOrDefault(key, MISSING);
    }

    /**
     * @return All textures that need to be loaded
     */
    public Collection<GeometryModelTexture[]> getTextures() {
        return this.textures.values();
    }

    /**
     * @return All definitions for textures
     */
    public Map<String, GeometryModelTexture[]> getTextureDefinitions() {
        return textures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeometryModelTextureTable that = (GeometryModelTextureTable) o;
        return textures.equals(that.textures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textures);
    }

    @Override
    public String toString() {
        return "GeometryModelTextureTable{" +
                "textures=" + textures +
                '}';
    }

    /**
     * Deserializes a new {@link GeometryModelTextureTable} from JSON.
     *
     * @author Ocelot
     * @deprecated {@link GeometryModelTextureTable#CODEC} serializes to/from JSON properly
     */
    @Deprecated
    public static class Serializer implements JsonSerializer<GeometryModelTextureTable>, JsonDeserializer<GeometryModelTextureTable> {
        private static final Logger LOGGER = LogManager.getLogger();

        @Override
        public JsonElement serialize(GeometryModelTextureTable src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject texturesObject = new JsonObject();
            for (Map.Entry<String, GeometryModelTexture[]> entry : src.textures.entrySet()) {
                GeometryModelTexture[] layers = entry.getValue();
                if (layers.length == 0)
                    continue;

                if (layers.length == 1) {
                    texturesObject.add(entry.getKey(), GeometryModelTexture.CODEC.encodeStart(JsonOps.INSTANCE, layers[0]).getOrThrow(false, LOGGER::error));
                    continue;
                }

                JsonArray layersJson = new JsonArray();
                for (GeometryModelTexture texture : layers)
                    layersJson.add(GeometryModelTexture.CODEC.encodeStart(JsonOps.INSTANCE, texture).getOrThrow(false, LOGGER::error));
                texturesObject.add(entry.getKey(), layersJson);
            }
            return texturesObject;
        }

        @Override
        public GeometryModelTextureTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject texturesObject = json.getAsJsonObject();
            Map<String, GeometryModelTexture[]> textures = new HashMap<>();
            List<GeometryModelTexture> textureSet = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : texturesObject.entrySet()) {
                try {
                    if (entry.getValue().isJsonArray()) {
                        JsonArray layersJson = entry.getValue().getAsJsonArray();
                        for (int i = 0; i < layersJson.size(); i++)
                            textureSet.add(GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, layersJson.get(i)).getOrThrow(false, LOGGER::error));
                        if (!textureSet.isEmpty()) {
                            textures.put(entry.getKey(), textureSet.toArray(new GeometryModelTexture[0]));
                            textureSet.clear();
                        }
                    } else {
                        textures.put(entry.getKey(), new GeometryModelTexture[]{GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, LOGGER::error)});
                    }
                } catch (Exception e) {
                    throw new JsonParseException("Failed to load texture '" + entry.getKey() + "'", e);
                }
            }
            return new GeometryModelTextureTable(textures);
        }
    }
}
