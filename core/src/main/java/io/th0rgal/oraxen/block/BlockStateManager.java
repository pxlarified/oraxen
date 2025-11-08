package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.events.CustomBlockStateUpdateEvent;
import io.th0rgal.oraxen.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStateManager {

    private static final String STATE_STORE_KEY = "oraxen_state";
    private static final String BLOCK_ID_KEY = "oraxen_id";
    private static final double SYNC_RANGE = 64.0;

    private final Map<String, Map<String, String>> blockStates = new ConcurrentHashMap<>();
    private final BlockManager blockManager;

    public BlockStateManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public void initializeBlockState(@NotNull Block block, @NotNull CustomBlock customBlock) {
        final Map<String, String> state = new HashMap<>();

        for (BlockProperty<?> property : customBlock.getProperties().getAllProperties()) {
            state.put(property.getName(), serializeProperty(property));
        }

        final String stateKey = getStateKey(block);
        blockStates.put(stateKey, new HashMap<>(state));
        
        persistStateToBlock(block, customBlock.getId().asString(), state);
    }

    @SuppressWarnings("unchecked")
    private <T> String serializeProperty(BlockProperty<T> property) {
        return property.valueToString(property.getDefaultValue());
    }

    public void clearBlockState(@NotNull Block block) {
        final String stateKey = getStateKey(block);
        blockStates.remove(stateKey);
        
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        pdc.remove(new org.bukkit.NamespacedKey("oraxen", STATE_STORE_KEY + "_" + stateKey));
        pdc.remove(new org.bukkit.NamespacedKey("oraxen", BLOCK_ID_KEY + "_" + stateKey));
    }

    @NotNull
    public Map<String, String> getBlockState(@NotNull Block block) {
        final String stateKey = getStateKey(block);
        
        Map<String, String> cached = blockStates.get(stateKey);
        if (cached != null) {
            return new HashMap<>(cached);
        }

        final CustomBlock customBlock = getCustomBlockAtLocation(block);
        if (customBlock == null) {
            return new HashMap<>();
        }

        final Map<String, String> state = loadStateFromBlock(block, customBlock);
        blockStates.put(stateKey, new HashMap<>(state));
        return state;
    }

    public void updateBlockProperty(@NotNull Block block, @NotNull String propertyName, @NotNull String value) {
        final CustomBlock customBlock = getCustomBlockAtLocation(block);
        if (customBlock == null) return;

        final BlockProperty<?> property = customBlock.getProperties().getProperty(propertyName);
        if (property == null) {
            return;
        }

        if (!isValidPropertyValue(property, value)) {
            return;
        }

        final String stateKey = getStateKey(block);
        final Map<String, String> previousState = new HashMap<>(blockStates.getOrDefault(stateKey, new HashMap<>()));
        final Map<String, String> newState = new HashMap<>(previousState);
        
        newState.put(propertyName, value);

        final CustomBlockStateUpdateEvent event = new CustomBlockStateUpdateEvent(customBlock, block, previousState, newState);
        EventUtils.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        blockStates.put(stateKey, newState);
        persistStateToBlock(block, customBlock.getId().asString(), newState);

        syncStateToNearbyPlayers(block, customBlock, newState);
        triggerNeighborUpdate(block, customBlock);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean isValidPropertyValue(BlockProperty<T> property, String stringValue) {
        T typedValue = property.stringToValue(stringValue);
        return property.isValidValue(typedValue);
    }

    @NotNull
    public String getBlockProperty(@NotNull Block block, @NotNull String propertyName) {
        final Map<String, String> state = getBlockState(block);
        return state.getOrDefault(propertyName, "");
    }

    public void setBlockProperty(@NotNull Block block, @NotNull String propertyName, @NotNull String value) {
        updateBlockProperty(block, propertyName, value);
    }

    private void persistStateToBlock(@NotNull Block block, @NotNull String blockId, @NotNull Map<String, String> state) {
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String stateKey = getStateKey(block);

        final StringBuilder stateData = new StringBuilder();
        for (Map.Entry<String, String> entry : state.entrySet()) {
            if (stateData.length() > 0) stateData.append("|");
            stateData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        pdc.set(
            new org.bukkit.NamespacedKey("oraxen", STATE_STORE_KEY + "_" + stateKey),
            PersistentDataType.STRING,
            stateData.toString()
        );
        pdc.set(
            new org.bukkit.NamespacedKey("oraxen", BLOCK_ID_KEY + "_" + stateKey),
            PersistentDataType.STRING,
            blockId
        );
    }

    @NotNull
    private Map<String, String> loadStateFromBlock(@NotNull Block block, @NotNull CustomBlock customBlock) {
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String stateKey = getStateKey(block);

        final String stateData = pdc.get(
            new org.bukkit.NamespacedKey("oraxen", STATE_STORE_KEY + "_" + stateKey),
            PersistentDataType.STRING
        );

        final Map<String, String> state = new HashMap<>();
        for (BlockProperty<?> property : customBlock.getProperties().getAllProperties()) {
            state.put(property.getName(), serializeProperty(property));
        }

        if (stateData != null && !stateData.isEmpty()) {
            final String[] pairs = stateData.split("\\|");
            for (String pair : pairs) {
                final String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    state.put(parts[0], parts[1]);
                }
            }
        }

        return state;
    }

    private void syncStateToNearbyPlayers(@NotNull Block block, @NotNull CustomBlock customBlock, @NotNull Map<String, String> state) {
        final Location blockLocation = block.getLocation();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != block.getWorld()) continue;
            
            if (player.getLocation().distanceSquared(blockLocation) <= SYNC_RANGE * SYNC_RANGE) {
                syncStateToPlayer(player, block, state);
            }
        }
    }

    private void syncStateToPlayer(@NotNull Player player, @NotNull Block block, @NotNull Map<String, String> state) {
        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    private void triggerNeighborUpdate(@NotNull Block block, @NotNull CustomBlock customBlock) {
        final BlockBehavior behavior = customBlock.getBehavior();
        if (behavior == null) return;

        final Block[] neighbors = {
            block.getRelative(0, 1, 0),
            block.getRelative(0, -1, 0),
            block.getRelative(1, 0, 0),
            block.getRelative(-1, 0, 0),
            block.getRelative(0, 0, 1),
            block.getRelative(0, 0, -1)
        };

        for (Block neighbor : neighbors) {
            final CustomBlock neighborBlock = getCustomBlockAtLocation(neighbor);
            if (neighborBlock != null) {
                final BlockBehavior neighborBehavior = neighborBlock.getBehavior();
                if (neighborBehavior != null) {
                    neighborBehavior.onNeighborUpdate(neighbor, block, neighborBlock);
                }
            }
        }
    }

    @Nullable
    private CustomBlock getCustomBlockAtLocation(@NotNull Block block) {
        if (block.getType() != Material.MUSHROOM_STEM) {
            return null;
        }

        for (CustomBlock customBlock : blockManager.getLoadedBlocks()) {
            if (isBlockAtLocationMatching(block, customBlock)) {
                return customBlock;
            }
        }

        return null;
    }

    public boolean isBlockAtLocationMatching(@NotNull Block block, @NotNull CustomBlock customBlock) {
        final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        final String stateKey = getStateKey(block);
        
        final String blockId = pdc.get(
            new org.bukkit.NamespacedKey("oraxen", BLOCK_ID_KEY + "_" + stateKey),
            PersistentDataType.STRING
        );

        return customBlock.getId().asString().equals(blockId);
    }

    @NotNull
    private String getStateKey(@NotNull Block block) {
        return String.format("%d_%d_%d", block.getX(), block.getY(), block.getZ());
    }
}
