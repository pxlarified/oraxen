package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ItemsAdderParser {

    public static class ContentFolderData {
        public final String folderName;
        public final Map<String, ItemsAdderItem> items;
        public final File texturesFolder;
        public final File modelsFolder;
        public final File soundsFolder;
        
        public ContentFolderData(String folderName, Map<String, ItemsAdderItem> items, 
                                File texturesFolder, File modelsFolder, File soundsFolder) {
            this.folderName = folderName;
            this.items = items;
            this.texturesFolder = texturesFolder;
            this.modelsFolder = modelsFolder;
            this.soundsFolder = soundsFolder;
        }
    }

    public static List<ContentFolderData> parseItemsAdderContents(@NotNull File itemsAdderFolder) {
        List<ContentFolderData> contentFolders = new ArrayList<>();
        
        if (!itemsAdderFolder.exists() || !itemsAdderFolder.isDirectory()) {
            Logs.logError("ItemsAdder folder does not exist or is not a directory: " + itemsAdderFolder.getAbsolutePath());
            return contentFolders;
        }
        
        File[] folders = itemsAdderFolder.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            Logs.logError("No content folders found in: " + itemsAdderFolder.getAbsolutePath());
            return contentFolders;
        }
        
        for (File folder : folders) {
            if (folder.getName().startsWith(".")) continue;
            
            Logs.logInfo("Processing content folder: " + folder.getName());
            ContentFolderData data = parseContentFolder(folder);
            if (data != null && !data.items.isEmpty()) {
                contentFolders.add(data);
            }
        }
        
        return contentFolders;
    }
    
    @Nullable
    private static ContentFolderData parseContentFolder(@NotNull File contentFolder) {
        String folderName = contentFolder.getName();
        Map<String, ItemsAdderItem> items = new HashMap<>();
        
        File configsFolder = new File(contentFolder, "configs");
        if (!configsFolder.exists()) {
            Logs.logWarning("  No configs folder found in: " + folderName);
            return null;
        }
        
        parseConfigsDirectory(configsFolder, items);
        
        if (items.isEmpty()) {
            Logs.logWarning("  No items found in: " + folderName);
            return null;
        }
        
        File texturesFolder = new File(contentFolder, "textures");
        File modelsFolder = new File(contentFolder, "models");
        File soundsFolder = new File(contentFolder, "sounds");
        
        Logs.logSuccess("  Found " + items.size() + " items in " + folderName);
        
        return new ContentFolderData(folderName, items, texturesFolder, modelsFolder, soundsFolder);
    }
    
    private static void parseConfigsDirectory(@NotNull File directory, @NotNull Map<String, ItemsAdderItem> items) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                parseConfigsDirectory(file, items);
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
                        ItemsAdderItem item = new ItemsAdderItem(key, itemSection);
                        items.put(key, item);
                    }
                }
            }
        } catch (Exception e) {
            Logs.logError("Failed to parse ItemsAdder file: " + file.getName());
            Logs.logError("Error: " + e.getMessage());
        }
    }
    
    public static void copyAssets(@NotNull File sourceFolder, @NotNull File targetFolder, @NotNull String assetType) throws IOException {
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            return;
        }
        
        Path sourcePath = sourceFolder.toPath();
        Path targetPath = targetFolder.toPath();
        
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }
        
        Files.walk(sourcePath).forEach(source -> {
            try {
                if (Files.isDirectory(source)) return;
                
                Path destination = targetPath.resolve(sourcePath.relativize(source));
                Path destinationParent = destination.getParent();
                if (destinationParent != null && !Files.exists(destinationParent)) {
                    Files.createDirectories(destinationParent);
                }
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logs.logError("Failed to copy " + assetType + ": " + source.getFileName());
            }
        });
    }
}
