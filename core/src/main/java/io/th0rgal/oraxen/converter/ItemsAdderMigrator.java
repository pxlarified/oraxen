package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ItemsAdderMigrator {

    private final File pluginDataFolder;
    
    public ItemsAdderMigrator() {
        this.pluginDataFolder = OraxenPlugin.get().getDataFolder();
    }
    
    public boolean migrate() {
        try {
            Logs.logInfo("Starting ItemsAdder migration...");
            Logs.logInfo("═══════════════════════════════════════");
            
            File converterFolder = new File(pluginDataFolder, "converter");
            File inputFolder = new File(converterFolder, "ItemsAdder");
            File outputFolder = new File(converterFolder, "output");
            
            if (!inputFolder.exists()) {
                inputFolder.mkdirs();
                Logs.logError("Input folder did not exist. Created: " + inputFolder.getAbsolutePath());
                Logs.logError("Please place your ItemsAdder content folders inside it and run the command again.");
                Logs.logError("Expected structure: converter/ItemsAdder/<content_folder>/configs/items.yml");
                return false;
            }
            
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            
            List<ItemsAdderParser.ContentFolderData> contentFolders = 
                ItemsAdderParser.parseItemsAdderContents(inputFolder);
            
            if (contentFolders.isEmpty()) {
                Logs.logError("No ItemsAdder content folders found in: " + inputFolder.getAbsolutePath());
                Logs.logError("Expected structure: converter/ItemsAdder/<content_folder>/configs/items.yml");
                return false;
            }
            
            int totalItems = 0;
            int totalTextures = 0;
            int totalModels = 0;
            int totalSounds = 0;
            int totalFonts = 0;
            File packOutputFolder = new File(outputFolder, "pack");
            
            for (ItemsAdderParser.ContentFolderData contentData : contentFolders) {
                Logs.logInfo("Converting: " + contentData.folderName);
                
                ItemsAdderConverter.convertToOraxen(contentData.folderName, contentData.items, contentData.fontImages, outputFolder);
                totalItems += contentData.items.size();
                
                if (contentData.texturesFolder.exists()) {
                    if (!packOutputFolder.exists()) {
                        packOutputFolder.mkdirs();
                    }
                    File targetTexturesFolder = new File(packOutputFolder, "textures");
                    try {
                        int count = copyTexturesExcludingFonts(contentData.texturesFolder, targetTexturesFolder);
                        totalTextures += count;
                        Logs.logInfo("  Copied " + count + " item textures");
                    } catch (IOException e) {
                        Logs.logError("  Failed to copy item textures: " + e.getMessage());
                    }
                }
                
                if (contentData.modelsFolder.exists()) {
                    if (!packOutputFolder.exists()) {
                        packOutputFolder.mkdirs();
                    }
                    File targetModelsFolder = new File(packOutputFolder, "models");
                    try {
                        ItemsAdderParser.copyAssets(contentData.modelsFolder, targetModelsFolder, "models");
                        int count = countFiles(contentData.modelsFolder);
                        totalModels += count;
                        Logs.logInfo("  Copied " + count + " models");
                    } catch (IOException e) {
                        Logs.logError("  Failed to copy models: " + e.getMessage());
                    }
                }
                
                if (contentData.soundsFolder.exists()) {
                    if (!packOutputFolder.exists()) {
                        packOutputFolder.mkdirs();
                    }
                    File targetSoundsFolder = new File(packOutputFolder, "sounds");
                    try {
                        ItemsAdderParser.copyAssets(contentData.soundsFolder, targetSoundsFolder, "sounds");
                        int count = countFiles(contentData.soundsFolder);
                        totalSounds += count;
                        Logs.logInfo("  Copied " + count + " sounds");
                    } catch (IOException e) {
                        Logs.logError("  Failed to copy sounds: " + e.getMessage());
                    }
                }
                
                if (contentData.fontFolder.exists()) {
                    if (!packOutputFolder.exists()) {
                        packOutputFolder.mkdirs();
                    }
                    File targetFontFolder = new File(packOutputFolder, "textures");
                    try {
                        ItemsAdderParser.copyAssets(contentData.fontFolder, targetFontFolder, "fonts");
                        int count = countFiles(contentData.fontFolder);
                        totalFonts += count;
                        Logs.logInfo("  Copied " + count + " font files");
                    } catch (IOException e) {
                        Logs.logError("  Failed to copy fonts: " + e.getMessage());
                    }
                }
            }
            
            Logs.logSuccess("═══════════════════════════════════════");
            Logs.logSuccess("ItemsAdder migration completed!");
            Logs.logSuccess("═══════════════════════════════════════");
            Logs.logSuccess("Statistics:");
            Logs.logSuccess("  Content folders processed: " + contentFolders.size());
            Logs.logSuccess("  Items converted: " + totalItems);
            Logs.logSuccess("  Glyphs converted: " + totalFonts);
            Logs.logSuccess("  Textures copied: " + totalTextures);
            Logs.logSuccess("  Models copied: " + totalModels);
            if (totalSounds > 0) {
                Logs.logSuccess("  Sounds copied: " + totalSounds);
            }
            Logs.logSuccess("═══════════════════════════════════════");
            Logs.logSuccess("Output location: " + outputFolder.getAbsolutePath());
            Logs.logInfo("  Items: " + new File(outputFolder, "items/").getAbsolutePath());
            Logs.logInfo("  Resource pack: " + packOutputFolder.getAbsolutePath());
            Logs.logSuccess("═══════════════════════════════════════");
            Logs.logInfo("Next steps:");
            Logs.logInfo("1. Review the converted items in: converter/output/items/");
            Logs.logInfo("2. Copy desired items to your Oraxen items/ folder");
            Logs.logInfo("3. Copy resource pack assets from converter/output/pack/ to your Oraxen pack/ folder");
            Logs.logInfo("4. Run /oraxen reload all to apply changes");
            
            return true;
            
        } catch (Exception e) {
            Logs.logError("Migration failed: " + e.getMessage());
            if (Settings.DEBUG.toBool()) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    private int countFiles(@NotNull File folder) {
        if (!folder.exists() || !folder.isDirectory()) return 0;
        
        int count = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFiles(file);
                }
            }
        }
        return count;
    }
    
    private int copyTexturesExcludingFonts(@NotNull File sourceFolder, @NotNull File targetFolder) throws IOException {
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            return 0;
        }
        
        File fontFolder = new File(sourceFolder, "font");
        int count = 0;
        File[] files = sourceFolder.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("font")) {
                    continue;
                }
                
                if (file.isDirectory()) {
                    count += countAndCopyDirectory(file, new File(targetFolder, file.getName()), fontFolder);
                } else {
                    count += countAndCopyFile(file, new File(targetFolder, file.getName()));
                }
            }
        }
        return count;
    }
    
    private int countAndCopyDirectory(@NotNull File source, @NotNull File target, @NotNull File fontFolderToExclude) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }
        
        int count = 0;
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countAndCopyDirectory(file, new File(target, file.getName()), fontFolderToExclude);
                } else {
                    count += countAndCopyFile(file, new File(target, file.getName()));
                }
            }
        }
        return count;
    }
    
    private int countAndCopyFile(@NotNull File source, @NotNull File target) throws IOException {
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return 1;
    }
}
