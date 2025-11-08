package io.th0rgal.oraxen.pack.generation;

import com.google.gson.JsonObject;
import io.th0rgal.oraxen.block.BlockAppearance;

public class BlockModelGenerator {

    private final JsonObject json = new JsonObject();

    public BlockModelGenerator(BlockAppearance appearance) {
        String parentModel = appearance.getParentModel();
        String texture = appearance.getTexture();

        if (parentModel != null && !parentModel.isEmpty()) {
            json.addProperty("parent", parentModel);
        } else {
            json.addProperty("parent", "block/cube_all");
        }

        if (texture != null && !texture.isEmpty()) {
            JsonObject textures = new JsonObject();
            textures.addProperty("all", texture);
            json.add("textures", textures);
        }
    }

    public JsonObject getJson() {
        return this.json;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }
}
