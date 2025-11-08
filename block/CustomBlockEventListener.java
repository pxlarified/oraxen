package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.events.CustomBlockBreakEvent;
import io.th0rgal.oraxen.api.events.CustomBlockPlaceEvent;
import io.th0rgal.oraxen.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CustomBlockEventListener implements Listener {

    private static final String STATE_KEY_PREFIX = "oraxen_state_";

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        final Player player = event.getPlayer();
        
        final CustomBlock customBlock = getCustomBlockAtLocation(block.getLocation());
        if (customBlock == null) return;

        final CustomBlockPlaceEvent customEvent = new CustomBlockPlaceEvent(customBlock, block, player, event);
        EventUtils.callEvent(customEvent);
        
        if (customEvent.isCancelled()) {
            return;
        }

        final BlockBehavior behavior = customBlock.getBehavior();
        if (behavior != null) {
            behavior.onPlace(event, customBlock);
        }

        initializeBlockState(block, customBlock);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        
        final CustomBlock customBlock = getCustomBlockAtLocation(block.getLocation());
        if (customBlock == null) return;

        final CustomBlockBreakEvent customEvent = new CustomBlockBreakEvent(customBlock, block, player, event);
        EventUtils.callEvent(customEvent);
        
        if (customEvent.isCancelled()) {
            return;
        }

        final BlockBehavior behavior = customBlock.getBehavior();
        if (behavior != null) {
            behavior.onBreak(event, customBlock);
        }

        clearBlockState(block);
    }

    private CustomBlock getCustomBlockAtLocation(@NotNull Location location) {
        final Block block = location.getBlock();
        if (block.getType() != Material.MUSHROOM_STEM) {
            return null;
        }

        final BlockManager blockManager = OraxenPlugin.get().getBlockManager();
        if (blockManager == null) return null;

        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String blockKey = getBlockLocationKey(block);
        
        final String customBlockId = pdc.get(
            new org.bukkit.NamespacedKey("oraxen", blockKey),
            PersistentDataType.STRING
        );

        if (customBlockId == null) return null;

        for (CustomBlock customBlock : blockManager.getLoadedBlocks()) {
            if (customBlock.getId().asString().equals(customBlockId)) {
                return customBlock;
            }
        }

        return null;
    }

    private void initializeBlockState(@NotNull Block block, @NotNull CustomBlock customBlock) {
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String blockKey = getBlockLocationKey(block);
        final org.bukkit.NamespacedKey stateKey = new org.bukkit.NamespacedKey("oraxen", blockKey);

        final Map<String, String> initialState = new HashMap<>();
        for (BlockProperty property : customBlock.getProperties().getProperties().values()) {
            initialState.put(property.getName(), property.getDefaultValue());
        }

        final StringBuilder stateData = new StringBuilder();
        for (Map.Entry<String, String> entry : initialState.entrySet()) {
            if (stateData.length() > 0) stateData.append("|");
            stateData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        pdc.set(stateKey, PersistentDataType.STRING, stateData.toString());
        pdc.set(new org.bukkit.NamespacedKey("oraxen", blockKey + "_id"), PersistentDataType.STRING, customBlock.getId().asString());
    }

    private void clearBlockState(@NotNull Block block) {
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String blockKey = getBlockLocationKey(block);
        
        pdc.remove(new org.bukkit.NamespacedKey("oraxen", blockKey));
        pdc.remove(new org.bukkit.NamespacedKey("oraxen", blockKey + "_id"));
    }

    @NotNull
    private String getBlockLocationKey(@NotNull Block block) {
        return String.format("block_%d_%d_%d", block.getX(), block.getY(), block.getZ());
    }
}
