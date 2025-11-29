package io.th0rgal.oraxen.converter;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ItemsAdderMigrator {

    private final File pluginDataFolder;
    
    public ItemsAdderMigrator() {
        this.pluginDataFolder = OraxenPlugin.get().getDataFolder();
    }
    
    public boolean migrate() {
        try {
            Logs.logInfo("Starting ItemsAdder migration...");
            
            File converterFolder = new File(pluginDataFolder, "converter");
            File inputFolder = new File(converterFolder, "ItemsAdder");
            File outputFolder = new File(converterFolder, "output");
            
            if (!inputFolder.exists()) {
                inputFolder.mkdirs();
                Logs.logError("Input folder did not exist. Created: " + inputFolder.getAbsolutePath());
                Logs.logError("Please place your ItemsAdder contents folder inside it and run the command again.");
                return false;
            }
            
            File[] contentsFolders = inputFolder.listFiles(File::isDirectory);
            if (contentsFolders == null || contentsFolders.length == 0) {
                Logs.logError("No ItemsAdder contents folders found in: " + inputFolder.getAbsolutePath());
                Logs.logError("Please place your ItemsAdder contents folder(s) inside it.");
                return false;
            }
            
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            
            File packOutputFolder = new File(outputFolder, "pack");
            if (!packOutputFolder.exists()) {
                packOutputFolder.mkdirs();
            }
            
            Map<String, ItemsAdderItem> allItems = new java.util.HashMap<>();
            
            for (File contentsFolder : contentsFolders) {
                Logs.logInfo("Processing contents folder: " + contentsFolder.getName());
                
                File configFolder = new File(contentsFolder, "configs");
                if (configFolder.exists()) {
                    Map<String, ItemsAdderItem> items = ItemsAdderParser.parseItemsAdderContents(configFolder);
                    allItems.putAll(items);
                } else {
                    Map<String, ItemsAdderItem> items = ItemsAdderParser.parseItemsAdderContents(contentsFolder);
                    allItems.putAll(items);
                }
                
                File resourcePackFolder = new File(contentsFolder, "resourcepack");
                if (resourcePackFolder.exists()) {
                    Logs.logInfo("Copying resource pack from: " + contentsFolder.getName());
                    try {
                        ItemsAdderParser.copyResourcePack(resourcePackFolder, packOutputFolder);
                        Logs.logSuccess("Resource pack copied successfully");
                    } catch (IOException e) {
                        Logs.logError("Failed to copy resource pack: " + e.getMessage());
                    }
                }
            }
            
            if (allItems.isEmpty()) {
                Logs.logError("No ItemsAdder items found to convert!");
                return false;
            }
            
            Logs.logInfo("Converting " + allItems.size() + " items to Oraxen format...");
            ItemsAdderConverter.convertToOraxen(allItems, outputFolder);
            
            Logs.logSuccess("═══════════════════════════════════════");
            Logs.logSuccess("ItemsAdder migration completed!");
            Logs.logSuccess("Output location: " + outputFolder.getAbsolutePath());
            Logs.logSuccess("Items config: " + new File(outputFolder, "items/converted_itemsadder.yml").getAbsolutePath());
            if (packOutputFolder.exists() && packOutputFolder.list() != null && packOutputFolder.list().length > 0) {
                Logs.logSuccess("Resource pack: " + packOutputFolder.getAbsolutePath());
            }
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
}
