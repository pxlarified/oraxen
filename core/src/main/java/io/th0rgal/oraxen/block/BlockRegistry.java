package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.utils.logs.Logs;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockRegistry {

    private final Map<Key, CustomBlock> blocks = new ConcurrentHashMap<>();
    private final Map<String, CustomBlock> blocksByString = new ConcurrentHashMap<>();

    public void register(@NotNull CustomBlock block) {
        if (block == null) return;
        
        Key id = block.getId();
        CustomBlock existing = blocks.put(id, block);
        blocksByString.put(id.asString(), block);
        
        if (existing != null) {
            Logs.logWarning("Block with ID '" + id + "' was already registered. Overwriting...");
        }
    }

    public void registerAll(@NotNull Collection<CustomBlock> blocks) {
        for (CustomBlock block : blocks) {
            register(block);
        }
    }

    @Nullable
    public CustomBlock getBlock(@NotNull Key id) {
        return blocks.get(id);
    }

    @Nullable
    public CustomBlock getBlock(@NotNull String id) {
        return blocksByString.get(id);
    }

    @NotNull
    public Optional<CustomBlock> getBlockOptional(@NotNull Key id) {
        return Optional.ofNullable(getBlock(id));
    }

    @NotNull
    public Optional<CustomBlock> getBlockOptional(@NotNull String id) {
        return Optional.ofNullable(getBlock(id));
    }

    @NotNull
    public Collection<CustomBlock> getLoadedBlocks() {
        return Collections.unmodifiableCollection(blocks.values());
    }

    @NotNull
    public Set<Key> getBlockIds() {
        return Collections.unmodifiableSet(blocks.keySet());
    }

    public boolean isRegistered(@NotNull Key id) {
        return blocks.containsKey(id);
    }

    public boolean isRegistered(@NotNull String id) {
        return blocksByString.containsKey(id);
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public void unregister(@NotNull Key id) {
        CustomBlock removed = blocks.remove(id);
        if (removed != null) {
            blocksByString.remove(id.asString());
        }
    }

    public void clear() {
        blocks.clear();
        blocksByString.clear();
    }

    @NotNull
    public List<String> getBlockNames() {
        return blocks.keySet().stream()
                .map(Key::asString)
                .sorted()
                .toList();
    }
}
