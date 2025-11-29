package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ItemsAdderParser {

    public static Map<String, ItemsAdderItem> parseItemsAdderContents(@NotNull File contentsFolder) {
        Map<String, ItemsAdderItem> items = new HashMap<>();
        
        if (!contentsFolder.exists() || !contentsFolder.isDirectory()) {
            Logs.logError("Contents folder does not exist or is not a directory: " + contentsFolder.getAbsolutePath());
            return items;
        }
        
        parseDirectory(contentsFolder, items);
        
        Logs.logSuccess("Parsed " + items.size() + " ItemsAdder items from " + contentsFolder.getName());
        return items;
    }
    
    private static void parseDirectory(@NotNull File directory, @NotNull Map<String, ItemsAdderItem> items) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                parseDirectory(file, items);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                parseYamlFile(file, items);
            }
        }
    }
    
    private static void parseYamlFile(@NotNull File file, @NotNull Map<String, ItemsAdderItem> items) {
        try {
            YamlConfiguration config = OraxenYaml.loadConfiguration(file);
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    if (itemSection != null) {
                        String fullId = key;
                        ItemsAdderItem item = new ItemsAdderItem(fullId, itemSection);
                        items.put(fullId, item);
                        Logs.logInfo("  Found item: " + fullId);
                    }
                }
            }
        } catch (Exception e) {
            Logs.logError("Failed to parse ItemsAdder file: " + file.getName());
            Logs.logError("Error: " + e.getMessage());
        }
    }
    
    public static void copyResourcePack(@NotNull File sourceFolder, @NotNull File targetFolder) throws IOException {
        if (!sourceFolder.exists()) {
            Logs.logWarning("Source folder does not exist: " + sourceFolder.getAbsolutePath());
            return;
        }
        
        Path sourcePath = sourceFolder.toPath();
        Path targetPath = targetFolder.toPath();
        
        Files.walk(sourcePath).forEach(source -> {
            try {
                Path destination = targetPath.resolve(sourcePath.relativize(source));
                if (Files.isDirectory(source)) {
                    if (!Files.exists(destination)) {
                        Files.createDirectories(destination);
                    }
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                Logs.logError("Failed to copy: " + source);
            }
        });
    }
}
