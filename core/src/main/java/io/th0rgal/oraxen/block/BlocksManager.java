package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.ConfigsManager;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BlocksManager {
    private final BlockManager blockManager;
    private final BlockParser blockParser;
    private final ConfigsManager configsManager;

    public BlocksManager(@NotNull ConfigsManager configsManager) {
        this.configsManager = configsManager;
        this.blockManager = new BlockManager();
        this.blockParser = new BlockParser(blockManager);
    }

    public void loadBlocks() {
        try {
            File blocksFile = new File(OraxenPlugin.get().getDataFolder(), "blocks.yml");
            if (!blocksFile.exists()) {
                Logs.logInfo("No blocks.yml found, skipping block system initialization");
                blockManager.initialize();
                return;
            }

            YamlConfiguration blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);
            ConfigurationSection blocksSection = blocksConfig.getConfigurationSection("blocks");

            blockParser.parseBlocksSection(blocksSection);
            blockManager.initialize();

            Logs.logInfo("Blocks loaded successfully");
        } catch (Exception e) {
            Logs.logError("Failed to load blocks: " + e.getMessage());
            if (io.th0rgal.oraxen.config.Settings.DEBUG.toBool()) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    public BlockManager getBlockManager() {
        return blockManager;
    }

    @Nullable
    public CustomBlock getBlock(@NotNull String id) {
        return blockManager.getBlock(id);
    }

    @NotNull
    public ImmutableBlockState getBlockState(int stateId) {
        return blockManager.getStateById(stateId);
    }

    public void reload() {
        blockManager.getBlockRegistry().clear();
        blockManager.getBlockRegistry().unfreeze();
        BlockRegistryMirror.reset();
        loadBlocks();
    }
}
