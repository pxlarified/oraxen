package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemsAdderParser {

    public static class ContentFolderData {
        public final String folderName;
        public final Map<String, ItemsAdderItem> items;
        public final Map<String, ItemsAdderFontImage> fontImages;
        public final File texturesFolder;
        public final File modelsFolder;
        public final File soundsFolder;
        public final File fontFolder;
        
        public ContentFolderData(String folderName, Map<String, ItemsAdderItem> items, 
                                Map<String, ItemsAdderFontImage> fontImages,
                                File texturesFolder, File modelsFolder, File soundsFolder, File fontFolder) {
            this.folderName = folderName;
            this.items = items;
            this.fontImages = fontImages;
            this.texturesFolder = texturesFolder;
            this.modelsFolder = modelsFolder;
            this.soundsFolder = soundsFolder;
            this.fontFolder = fontFolder;
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
            if (data != null && (!data.items.isEmpty() || !data.fontImages.isEmpty())) {
                contentFolders.add(data);
            }
        }
        
        return contentFolders;
    }
    
    @Nullable
    private static ContentFolderData parseContentFolder(@NotNull File contentFolder) {
        String folderName = contentFolder.getName();
        Map<String, ItemsAdderItem> items = new HashMap<>();
        Map<String, ItemsAdderFontImage> fontImages = new HashMap<>();
        
        File configsFolder = new File(contentFolder, "configs");
        if (!configsFolder.exists()) {
            Logs.logWarning("  No configs folder found in: " + folderName);
            return null;
        }
        
        parseConfigsDirectory(configsFolder, items, fontImages);
        
        if (items.isEmpty() && fontImages.isEmpty()) {
            Logs.logWarning("  No items or font images found in: " + folderName);
            return null;
        }
        
        File texturesFolder = new File(contentFolder, "textures");
        File modelsFolder = new File(contentFolder, "models");
        File soundsFolder = new File(contentFolder, "sounds");
        File fontFolder = new File(texturesFolder, "font");
        
        if (!items.isEmpty()) {
            Logs.logSuccess("  Found " + items.size() + " items in " + folderName);
        }
        if (!fontImages.isEmpty()) {
            Logs.logSuccess("  Found " + fontImages.size() + " font images in " + folderName);
        }
        
        return new ContentFolderData(folderName, items, fontImages, texturesFolder, modelsFolder, soundsFolder, fontFolder);
    }
    
    private static void parseConfigsDirectory(@NotNull File directory, @NotNull Map<String, ItemsAdderItem> items, 
                                               @NotNull Map<String, ItemsAdderFontImage> fontImages) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                parseConfigsDirectory(file, items, fontImages);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                parseYamlFile(file, items, fontImages);
            }
        }
    }
    
    private static void parseYamlFile(@NotNull File file, @NotNull Map<String, ItemsAdderItem> items,
                                      @NotNull Map<String, ItemsAdderFontImage> fontImages) {
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
            
            ConfigurationSection fontImagesSection = config.getConfigurationSection("font_images");
            if (fontImagesSection != null) {
                for (String key : fontImagesSection.getKeys(false)) {
                    ConfigurationSection fontImageSection = fontImagesSection.getConfigurationSection(key);
                    if (fontImageSection != null) {
                        ItemsAdderFontImage fontImage = new ItemsAdderFontImage(key, fontImageSection);
                        fontImages.put(key, fontImage);
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
                
                if ("models".equals(assetType) && source.toString().endsWith(".json")) {
                    processModelFile(source.toFile(), destination.toFile());
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                Logs.logError("Failed to copy " + assetType + ": " + source.getFileName());
            }
        });
    }
    
    private static void processModelFile(@NotNull File sourceFile, @NotNull File destinationFile) throws IOException {
        String content = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        
        Pattern pattern = Pattern.compile(":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String texturePath = matcher.group(1);
            if (texturePath.startsWith("#")) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(": \"" + texturePath + "\""));
                continue;
            }
            String strippedPath = stripNamespace(texturePath);
            String formattedPath = formatTexturePath(strippedPath);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(": \"" + formattedPath + "\""));
        }
        matcher.appendTail(sb);
        
        Files.createDirectories(destinationFile.toPath().getParent());
        Files.writeString(destinationFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
    
    private static String stripNamespace(@NotNull String path) {
        if (path.contains(":")) {
            return path.substring(path.indexOf(":") + 1);
        }
        return path;
    }
    
    private static String formatTexturePath(@NotNull String path) {
        if (path.startsWith("item/")) {
            return path.substring(5);
        }
        if (path.startsWith("font/")) {
            return path.substring(5);
        }
        return path;
    }
}
