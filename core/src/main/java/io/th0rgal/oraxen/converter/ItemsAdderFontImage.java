package io.th0rgal.oraxen.converter;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderFontImage {
    
    private final String id;
    private final ConfigurationSection section;
    
    public ItemsAdderFontImage(@NotNull String id, @NotNull ConfigurationSection section) {
        this.id = id;
        this.section = section;
    }
    
    @NotNull
    public String getId() {
        return id;
    }
    
    @Nullable
    public String getPath() {
        return section.getString("path");
    }
    
    @Nullable
    public String getPermission() {
        return section.getString("permission");
    }
    
    public boolean shouldShowInGui() {
        return section.getBoolean("show_in_gui", true);
    }
    
    public int getScaleRatio() {
        return section.getInt("scale_ratio", 10);
    }
    
    public int getYPosition() {
        return section.getInt("y_position", 0);
    }
    
    public boolean hasShadow() {
        ConfigurationSection shadow = section.getConfigurationSection("shadow");
        if (shadow == null) return true;
        return shadow.getBoolean("enabled", true);
    }
    
    @NotNull
    public ConfigurationSection getRawSection() {
        return section;
    }
}
