package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class StringEnumProperty extends Property<String> {
    private final String[] values;

    StringEnumProperty(@NotNull String name, @NotNull String[] values) {
        super(name, String.class);
        this.values = values;
    }

    @Override
    public String getDefaultValue() {
        return values.length > 0 ? values[0] : "";
    }

    @Override
    public String[] getPossibleValues() {
        return values;
    }
}

public class BlockParser {

    private final BlockManager blockManager;

    public BlockParser(@NotNull BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public void parseBlocksSection(@Nullable ConfigurationSection blocksSection) {
        if (blocksSection == null) {
            Logs.logInfo("No blocks section found in configuration");
            return;
        }

        int loadedCount = 0;
        for (String blockId : blocksSection.getKeys(false)) {
            try {
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(blockId);
                if (blockSection == null) continue;

                parseBlock(blockId, blockSection);
                loadedCount++;
            } catch (Exception e) {
                Logs.logError("Failed to parse block '" + blockId + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        Logs.logInfo(String.format("Loaded %d custom blocks", loadedCount));
    }

    private void parseBlock(@NotNull String blockId, @NotNull ConfigurationSection blockSection) {
        Property<?>[] properties = parseProperties(blockSection.getConfigurationSection("properties"));
        BlockSettings settings = parseSettings(blockSection.getConfigurationSection("settings"));
        
        String visualBlockStr = blockSection.getString("visual_block");
        if (visualBlockStr != null) {
            try {
                Material visualMaterial = Material.valueOf(visualBlockStr.toUpperCase());
                settings.setVisualMaterial(visualMaterial);
            } catch (IllegalArgumentException e) {
                Logs.logWarning("Unknown material for visual_block: " + visualBlockStr);
            }
        }

        SimpleCustomBlock block = new SimpleCustomBlock(blockId, properties, settings);
        blockManager.registerBlock(block);
    }

    @NotNull
    private Property<?>[] parseProperties(@Nullable ConfigurationSection propertiesSection) {
        if (propertiesSection == null) {
            return new Property<?>[0];
        }

        List<Property<?>> properties = new ArrayList<>();

        for (String propertyName : propertiesSection.getKeys(false)) {
            ConfigurationSection propSection = propertiesSection.getConfigurationSection(propertyName);
            if (propSection == null) continue;

            String type = propSection.getString("type", "boolean");

            switch (type.toLowerCase()) {
                case "boolean":
                    properties.add(new Property.BooleanProperty(propertyName));
                    break;
                case "integer":
                    int min = propSection.getInt("min", 0);
                    int max = propSection.getInt("max", 15);
                    properties.add(new Property.IntProperty(propertyName, min, max));
                    break;
                case "direction":
                    List<String> directions = propSection.getStringList("values");
                    if (!directions.isEmpty()) {
                        Property.Direction[] directionValues = directions.stream()
                                .map(d -> Property.Direction.valueOf(d.toUpperCase()))
                                .toArray(Property.Direction[]::new);
                        properties.add(new Property.DirectionProperty(propertyName, directionValues));
                    } else {
                        properties.add(new Property.DirectionProperty(propertyName));
                    }
                    break;
                case "enum":
                    List<String> enumValues = propSection.getStringList("values");
                    if (!enumValues.isEmpty()) {
                        properties.add(createStringEnumProperty(propertyName, enumValues));
                    }
                    break;
                default:
                    Logs.logWarning("Unknown property type: " + type);
            }
        }

        return properties.toArray(new Property<?>[0]);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Property<?> createStringEnumProperty(@NotNull String name, @NotNull List<String> values) {
        return new StringEnumProperty(name, values.toArray(new String[0]));
    }

    @NotNull
    private BlockSettings parseSettings(@Nullable ConfigurationSection settingsSection) {
        BlockSettings settings = new BlockSettings();

        if (settingsSection == null) {
            return settings;
        }

        settings.setHardness((float) settingsSection.getDouble("hardness", 1.0f));
        settings.setResistance((float) settingsSection.getDouble("resistance", 1.0f));
        settings.setFriction((float) settingsSection.getDouble("friction", 0.6f));
        settings.setSpeedFactor((float) settingsSection.getDouble("speed_factor", 1.0f));
        settings.setJumpFactor((float) settingsSection.getDouble("jump_factor", 1.0f));
        settings.setLuminance(settingsSection.getInt("luminance", 0));
        settings.setBurnable(settingsSection.getBoolean("burnable", false));
        settings.setReplaceable(settingsSection.getBoolean("replaceable", false));
        settings.setRequiresCorrectTool(settingsSection.getBoolean("requires_correct_tool", false));
        settings.setRandomTicks(settingsSection.getBoolean("random_ticks", false));

        return settings;
    }
}
