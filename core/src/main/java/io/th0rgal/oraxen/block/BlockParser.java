package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.utils.logs.Logs;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockParser {

    private final String blockId;
    private final ConfigurationSection section;

    public BlockParser(@NotNull String blockId, @NotNull ConfigurationSection section) {
        this.blockId = blockId;
        this.section = section;
    }

    @Nullable
    public CustomBlock parse() {
        try {
            String namespace = section.getString("namespace", "oraxen");
            Key blockKey = Key.key(namespace, blockId);

            BlockSettings settings = parseSettings();
            BlockProperties properties = parseProperties();
            BlockAppearance appearance = parseAppearance();
            BlockBehavior behavior = parseBehavior();

            return new SimpleCustomBlock(blockKey, settings, properties, appearance, behavior);
        } catch (Exception e) {
            Logs.logError("Failed to parse block '" + blockId + "': " + e.getMessage());
            if (e.getMessage() != null) {
                Logs.logWarning(e.getMessage());
            }
            return null;
        }
    }

    @NotNull
    private BlockSettings parseSettings() {
        BlockSettings.Builder builder = new BlockSettings.Builder();

        String materialStr = section.getString("material", "PAPER");
        try {
            Material material = Material.valueOf(materialStr);
            builder.material(material);
        } catch (IllegalArgumentException e) {
            Logs.logWarning("Invalid material '" + materialStr + "' for block '" + blockId + "'. Using PAPER.");
        }

        ConfigurationSection settingsSection = section.getConfigurationSection("settings");
        if (settingsSection != null) {
            builder.hardness((float) settingsSection.getDouble("hardness", 1.0f));
            builder.resistance((float) settingsSection.getDouble("resistance", 1.0f));
            builder.replaceable(settingsSection.getBoolean("replaceable", false));
            builder.requiresCorrectToolForDrops(settingsSection.getBoolean("requires_correct_tool_for_drops", false));
            builder.emitLight(settingsSection.getInt("emit_light", 0));
            builder.soundType(settingsSection.getString("sound_type", "wood"));
        }

        return builder.build();
    }

    @NotNull
    private BlockProperties parseProperties() {
        BlockProperties properties = new BlockProperties();
        ConfigurationSection propertiesSection = section.getConfigurationSection("properties");

        if (propertiesSection != null) {
            for (String propertyName : propertiesSection.getKeys(false)) {
                Object value = propertiesSection.get(propertyName);

                if (value instanceof List<?> list) {
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) list;
                    properties.addProperty(new EnumProperty(propertyName, values));
                } else if (value instanceof String str) {
                    if (str.equalsIgnoreCase("boolean")) {
                        properties.addProperty(new BooleanProperty(propertyName));
                    } else {
                        try {
                            String[] parts = str.split(":");
                            if (parts.length == 2) {
                                int min = Integer.parseInt(parts[0]);
                                int max = Integer.parseInt(parts[1]);
                                properties.addProperty(new IntegerProperty(propertyName, min, max));
                            }
                        } catch (NumberFormatException e) {
                            Logs.logWarning("Invalid property definition for '" + propertyName + "': " + str);
                        }
                    }
                }
            }
        }

        return properties;
    }

    @NotNull
    private BlockAppearance parseAppearance() {
        ConfigurationSection appearanceSection = section.getConfigurationSection("appearance");
        BlockAppearance.Builder builder = new BlockAppearance.Builder();

        if (appearanceSection != null) {
            builder.modelName(appearanceSection.getString("model_name", blockId));
            builder.parentModel(appearanceSection.getString("parent_model", "block/cube_all"));
            builder.texture(appearanceSection.getString("texture"));
            builder.generateModel(appearanceSection.getBoolean("generate_model", false));
            builder.customRender(appearanceSection.getBoolean("custom_render", false));
        } else {
            builder.modelName(blockId);
        }

        return builder.build();
    }

    @Nullable
    private BlockBehavior parseBehavior() {
        ConfigurationSection behaviorSection = section.getConfigurationSection("behavior");
        if (behaviorSection == null) return null;

        return new BlockBehavior() {
        };
    }
}
