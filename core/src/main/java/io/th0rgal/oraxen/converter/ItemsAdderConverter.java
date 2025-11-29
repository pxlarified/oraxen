package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemsAdderConverter {

    public static void convertToOraxen(@NotNull String folderName,
                                        @NotNull Map<String, ItemsAdderItem> itemsAdderItems, 
                                        @NotNull File outputFolder) throws IOException {
        
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        
        File itemsFolder = new File(outputFolder, "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }
        
        YamlConfiguration oraxenConfig = new YamlConfiguration();
        int convertedCount = 0;
        int skippedCount = 0;
        
        for (Map.Entry<String, ItemsAdderItem> entry : itemsAdderItems.entrySet()) {
            ItemsAdderItem iaItem = entry.getValue();
            
            if (!iaItem.isEnabled()) {
                Logs.logWarning("  Skipping disabled item: " + iaItem.getId());
                skippedCount++;
                continue;
            }
            
            try {
                convertItem(oraxenConfig, iaItem);
                convertedCount++;
            } catch (Exception e) {
                Logs.logError("  Failed to convert item: " + iaItem.getId());
                Logs.logError("  Error: " + e.getMessage());
                skippedCount++;
            }
        }
        
        File outputFile = new File(itemsFolder, folderName + "_converted.yml");
        oraxenConfig.save(outputFile);
        
        Logs.logSuccess("  Converted " + convertedCount + " items from " + folderName);
        if (skippedCount > 0) {
            Logs.logWarning("  Skipped " + skippedCount + " items");
        }
    }
    
    private static void convertItem(@NotNull YamlConfiguration config, @NotNull ItemsAdderItem iaItem) {
        String itemId = iaItem.getId().replace(":", "_");
        
        String displayName = iaItem.getDisplayName();
        if (displayName != null) {
            config.set(itemId + ".displayname", displayName);
        }
        
        List<String> lore = iaItem.getLore();
        if (lore != null && !lore.isEmpty()) {
            config.set(itemId + ".lore", lore);
        }
        
        String material = iaItem.getResourceMaterial();
        if (material != null) {
            config.set(itemId + ".material", material.toUpperCase());
        }
        
        boolean shouldGenerate = iaItem.shouldGenerateModel();
        String modelPath = iaItem.getResourceModel();
        List<String> textures = iaItem.getResourceTextures();
        
        if (modelPath != null || (textures != null && !textures.isEmpty())) {
            config.set(itemId + ".Pack.generate_model", shouldGenerate);
            
            if (shouldGenerate) {
                config.set(itemId + ".Pack.parent_model", "item/generated");
                
                if (textures != null && !textures.isEmpty()) {
                    config.set(itemId + ".Pack.textures", textures);
                } else if (modelPath != null) {
                    List<String> inferredTextures = new ArrayList<>();
                    inferredTextures.add("item/" + modelPath);
                    config.set(itemId + ".Pack.textures", inferredTextures);
                }
            } else if (modelPath != null) {
                config.set(itemId + ".Pack.model", modelPath);
            }
        }
        
        Map<String, Integer> enchants = iaItem.getEnchants();
        if (enchants != null && !enchants.isEmpty()) {
            for (Map.Entry<String, Integer> enchant : enchants.entrySet()) {
                config.set(itemId + ".Enchantments." + enchant.getKey(), enchant.getValue());
            }
        }
        
        ConfigurationSection durability = iaItem.getDurability();
        if (durability != null) {
            int maxDurability = durability.getInt("max_durability", -1);
            if (maxDurability > 0) {
                config.set(itemId + ".Mechanics.durability.value", maxDurability);
            }
        }
        
        ConfigurationSection attributeModifiers = iaItem.getAttributeModifiers();
        if (attributeModifiers != null) {
            convertAttributeModifiers(config, itemId, attributeModifiers);
        }
        
        List<String> itemFlags = iaItem.getItemFlags();
        if (itemFlags != null && !itemFlags.isEmpty()) {
            config.set(itemId + ".ItemFlags", itemFlags);
        }
        
        String permissionSuffix = iaItem.getPermissionSuffix();
        if (permissionSuffix != null) {
            config.set(itemId + ".permission", "oraxen.item." + permissionSuffix);
        }
        
        ConfigurationSection behaviours = iaItem.getBehaviours();
        if (behaviours != null) {
            convertBehaviours(config, itemId, behaviours);
        }
    }
    
    private static void convertAttributeModifiers(@NotNull YamlConfiguration config, 
                                                    @NotNull String itemId,
                                                    @NotNull ConfigurationSection attributeModifiers) {
        
        for (String slot : attributeModifiers.getKeys(false)) {
            ConfigurationSection slotSection = attributeModifiers.getConfigurationSection(slot);
            if (slotSection == null) continue;
            
            for (String attribute : slotSection.getKeys(false)) {
                double value = slotSection.getDouble(attribute);
                String oraxenAttribute = convertAttributeName(attribute);
                
                config.set(itemId + ".AttributeModifiers", 
                    config.getList(itemId + ".AttributeModifiers", new ArrayList<>()));
                
                List<Map<String, Object>> modifiers = 
                    (List<Map<String, Object>>) config.getList(itemId + ".AttributeModifiers");
                
                Map<String, Object> modifier = new HashMap<>();
                modifier.put("attribute", oraxenAttribute.toUpperCase());
                modifier.put("amount", value);
                modifier.put("operation", 0);
                modifier.put("slot", slot.equalsIgnoreCase("mainhand") ? "HAND" : slot.toUpperCase());
                
                modifiers.add(modifier);
                config.set(itemId + ".AttributeModifiers", modifiers);
            }
        }
    }
    
    private static String convertAttributeName(@NotNull String iaAttribute) {
        return switch (iaAttribute.toLowerCase()) {
            case "attackdamage" -> "GENERIC_ATTACK_DAMAGE";
            case "attackspeed" -> "GENERIC_ATTACK_SPEED";
            case "maxhealth" -> "GENERIC_MAX_HEALTH";
            case "movementspeed" -> "GENERIC_MOVEMENT_SPEED";
            case "armor" -> "GENERIC_ARMOR";
            case "armortoughness" -> "GENERIC_ARMOR_TOUGHNESS";
            case "attackknockback" -> "GENERIC_ATTACK_KNOCKBACK";
            case "luck" -> "GENERIC_LUCK";
            default -> iaAttribute.toUpperCase();
        };
    }
    
    private static void convertBehaviours(@NotNull YamlConfiguration config,
                                          @NotNull String itemId,
                                          @NotNull ConfigurationSection behaviours) {
        
        if (behaviours.contains("furniture")) {
            ConfigurationSection furniture = behaviours.getConfigurationSection("furniture");
            if (furniture != null) {
                config.set(itemId + ".Mechanics.furniture.type", "DISPLAY_ENTITY");
                
                String modelPath = furniture.getString("model_path");
                if (modelPath != null) {
                    config.set(itemId + ".Mechanics.furniture.displayEntityProperties.model", modelPath);
                }
                
                double width = furniture.getDouble("hitbox.length", 1.0);
                double height = furniture.getDouble("hitbox.height", 1.0);
                config.set(itemId + ".Mechanics.furniture.barrier", true);
                config.set(itemId + ".Mechanics.furniture.barrierWidth", width);
                config.set(itemId + ".Mechanics.furniture.barrierHeight", height);
            }
        }
        
        if (behaviours.contains("block")) {
            ConfigurationSection block = behaviours.getConfigurationSection("block");
            if (block != null) {
                config.set(itemId + ".Mechanics.noteblock.custom_variation", 1);
                config.set(itemId + ".Mechanics.noteblock.model", itemId);
                
                boolean placeable = block.getBoolean("placeable", true);
                if (placeable) {
                    config.set(itemId + ".Mechanics.noteblock.place_permission", "oraxen.place." + itemId);
                }
            }
        }
    }
}
