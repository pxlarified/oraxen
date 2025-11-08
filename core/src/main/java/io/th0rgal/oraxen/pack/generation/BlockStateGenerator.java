package io.th0rgal.oraxen.pack.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.th0rgal.oraxen.block.BlockAppearance;
import io.th0rgal.oraxen.block.BlockProperties;
import io.th0rgal.oraxen.block.BlockProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStateGenerator {

    private final JsonObject json = new JsonObject();

    public BlockStateGenerator(String modelName, BlockProperties properties, BlockAppearance appearance) {
        Collection<BlockProperty<?>> blockProperties = properties.getAllProperties();

        if (blockProperties.isEmpty()) {
            JsonArray variants = new JsonArray();
            JsonObject variant = createModelVariant(modelName);
            variants.add(variant);
            json.add("variants", variants);
        } else {
            generateVariantsWithProperties(modelName, blockProperties);
        }
    }

    private void generateVariantsWithProperties(String modelName, Collection<BlockProperty<?>> properties) {
        JsonObject variants = new JsonObject();

        List<Map<String, String>> propertyValuesList = generatePropertyCombinations(properties);

        for (Map<String, String> propValues : propertyValuesList) {
            String key = buildPropertyKey(propValues);
            JsonObject variant = createModelVariant(modelName);
            variants.add(key, variant);
        }

        json.add("variants", variants);
    }

    private List<Map<String, String>> generatePropertyCombinations(Collection<BlockProperty<?>> properties) {
        List<Map<String, String>> combinations = new ArrayList<>();
        List<BlockProperty<?>> propList = new ArrayList<>(properties);

        generateCombinations(propList, 0, new HashMap<>(), combinations);

        return combinations;
    }

    private void generateCombinations(List<BlockProperty<?>> properties, int index, 
                                     Map<String, String> current, List<Map<String, String>> result) {
        if (index == properties.size()) {
            result.add(new HashMap<>(current));
            return;
        }

        BlockProperty<?> prop = properties.get(index);
        for (Object value : prop.getPossibleValues()) {
            current.put(prop.getName(), valueToStringUnchecked(prop, value));
            generateCombinations(properties, index + 1, current, result);
            current.remove(prop.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> String valueToStringUnchecked(BlockProperty<T> prop, Object value) {
        return prop.valueToString((T) value);
    }

    private String buildPropertyKey(Map<String, String> propValues) {
        StringBuilder sb = new StringBuilder();
        propValues.forEach((key, value) -> {
            if (!sb.isEmpty()) {
                sb.append(",");
            }
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }

    private JsonObject createModelVariant(String modelName) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", "oraxen/block/" + modelName);
        return variant;
    }

    public JsonObject getJson() {
        return this.json;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }
}
