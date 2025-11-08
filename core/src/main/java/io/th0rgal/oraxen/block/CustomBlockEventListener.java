package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.events.CustomBlockBreakEvent;
import io.th0rgal.oraxen.api.events.CustomBlockPlaceEvent;
import io.th0rgal.oraxen.utils.EventUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockEventListener implements Listener {

    private final BlockStateManager stateManager;

    public CustomBlockEventListener(BlockStateManager stateManager) {
        this.stateManager = stateManager;
    }

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

        stateManager.initializeBlockState(block, customBlock);
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

        stateManager.clearBlockState(block);
    }

    @Nullable
    private CustomBlock getCustomBlockAtLocation(@NotNull Location location) {
        final Block block = location.getBlock();

        final BlockManager blockManager = OraxenPlugin.get().getBlockManager();
        if (blockManager == null) return null;

        for (CustomBlock customBlock : blockManager.getLoadedBlocks()) {
            if (block.getType() == customBlock.getSettings().getMaterial()) {
                if (stateManager.isBlockAtLocationMatching(block, customBlock)) {
                    return customBlock;
                }
            }
        }

        return null;
    }
}
