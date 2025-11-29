package io.th0rgal.oraxen.converter;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemsAdderItem {
    
    private final String id;
    private final ConfigurationSection section;
    
    public ItemsAdderItem(@NotNull String id, @NotNull ConfigurationSection section) {
        this.id = id;
        this.section = section;
    }
    
    @NotNull
    public String getId() {
        return id;
    }
    
    @Nullable
    public String getDisplayName() {
        String name = section.getString("display_name");
        if (name == null) name = section.getString("name");
        return name;
    }
    
    @Nullable
    public List<String> getLore() {
        return section.getStringList("lore");
    }
    
    @Nullable
    public String getResourceMaterial() {
        ConfigurationSection resource = section.getConfigurationSection("resource");
        if (resource == null) return null;
        return resource.getString("material");
    }
    
    @Nullable
    public String getResourceModel() {
        ConfigurationSection resource = section.getConfigurationSection("resource");
        if (resource == null) return null;
        return resource.getString("model_path");
    }
    
    @Nullable
    public List<String> getResourceTextures() {
        ConfigurationSection resource = section.getConfigurationSection("resource");
        if (resource == null) return null;
        return resource.getStringList("textures");
    }
    
    public boolean shouldGenerateModel() {
        ConfigurationSection resource = section.getConfigurationSection("resource");
        if (resource == null) return false;
        return resource.getBoolean("generate", true);
    }
    
    @Nullable
    public Map<String, Integer> getEnchants() {
        ConfigurationSection enchants = section.getConfigurationSection("enchants");
        if (enchants == null) {
            List<String> enchantsList = section.getStringList("enchants");
            if (!enchantsList.isEmpty()) {
                Map<String, Integer> result = new HashMap<>();
                for (String enchant : enchantsList) {
                    String[] parts = enchant.split(":");
                    if (parts.length == 2) {
                        result.put(parts[0], Integer.parseInt(parts[1]));
                    }
                }
                return result;
            }
            return null;
        }
        
        Map<String, Integer> result = new HashMap<>();
        for (String key : enchants.getKeys(false)) {
            result.put(key, enchants.getInt(key));
        }
        return result;
    }
    
    @Nullable
    public ConfigurationSection getDurability() {
        return section.getConfigurationSection("durability");
    }
    
    @Nullable
    public ConfigurationSection getAttributeModifiers() {
        return section.getConfigurationSection("attribute_modifiers");
    }
    
    @Nullable
    public List<String> getItemFlags() {
        return section.getStringList("item_flags");
    }
    
    public boolean isEnabled() {
        return section.getBoolean("enabled", true);
    }
    
    @Nullable
    public String getPermissionSuffix() {
        return section.getString("permission_suffix");
    }
    
    @Nullable
    public ConfigurationSection getBehaviours() {
        return section.getConfigurationSection("behaviours");
    }
    
    @NotNull
    public ConfigurationSection getRawSection() {
        return section;
    }
}
