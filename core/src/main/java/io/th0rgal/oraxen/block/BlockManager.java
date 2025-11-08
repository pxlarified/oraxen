package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class BlockManager {

    private final BlockRegistry registry;
    private final BlockStateManager stateManager;
    private final File blocksFolder;

    public BlockManager() {
        this.registry = new BlockRegistry();
        this.stateManager = new BlockStateManager(this);
        this.blocksFolder = new File(OraxenPlugin.get().getDataFolder(), "blocks");
    }

    public void init() {
        if (!blocksFolder.exists()) {
            blocksFolder.mkdirs();
            if (Settings.GENERATE_DEFAULT_CONFIGS.toBool()) {
                Logs.logInfo("Creating default blocks folder...");
            }
        }
    }

    public void load() {
        try {
            registry.clear();
            List<File> blockFiles = getBlockFiles();
            
            if (blockFiles.isEmpty()) {
                Logs.logInfo("No custom blocks found.");
                return;
            }

            int successCount = 0;
            for (File file : blockFiles) {
                YamlConfiguration config = OraxenYaml.loadConfiguration(file);
                for (String blockKey : config.getKeys(false)) {
                    ConfigurationSection blockSection = config.getConfigurationSection(blockKey);
                    if (blockSection == null) continue;

                    BlockParser parser = new BlockParser(blockKey, blockSection);
                    CustomBlock block = parser.parse();
                    
                    if (block != null) {
                        registry.register(block);
                        successCount++;
                    }
                }
            }

            Logs.logInfo("Loaded " + successCount + " custom blocks.");
        } catch (Exception e) {
            Logs.logError("Failed to load custom blocks!");
            if (Settings.DEBUG.toBool()) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        load();
    }

    @NotNull
    public BlockRegistry getRegistry() {
        return registry;
    }

    @NotNull
    public Collection<CustomBlock> getLoadedBlocks() {
        return registry.getLoadedBlocks();
    }

    @NotNull
    public List<File> getBlockFiles() {
        if (!blocksFolder.exists()) {
            return new ArrayList<>();
        }
        
        Collection<File> files = FileUtils.listFiles(blocksFolder, new String[]{"yml"}, true);
        return files.stream()
                .filter(OraxenYaml::isValidYaml)
                .sorted()
                .toList();
    }

    public int getBlockCount() {
        return registry.getBlockCount();
    }

    @NotNull
    public BlockStateManager getStateManager() {
        return stateManager;
    }
}
