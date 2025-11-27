package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockRegistry {
    private final Map<String, CustomBlock> blocksById = new LinkedHashMap<>();
    private final List<ImmutableBlockState> allBlockStates = new ArrayList<>();
    private volatile boolean frozen = false;

    public void register(@NotNull CustomBlock block) {
        if (frozen) {
            throw new IllegalStateException("Cannot register block after registry is frozen");
        }

        if (blocksById.containsKey(block.getId())) {
            throw new IllegalArgumentException("Block with ID '" + block.getId() + "' already registered");
        }

        blocksById.put(block.getId(), block);
        allBlockStates.addAll(block.getStates());
    }

    @Nullable
    public CustomBlock getBlock(@NotNull String id) {
        return blocksById.get(id);
    }

    @NotNull
    public Collection<CustomBlock> getAllBlocks() {
        return Collections.unmodifiableCollection(blocksById.values());
    }

    @NotNull
    public List<ImmutableBlockState> getAllBlockStates() {
        return Collections.unmodifiableList(allBlockStates);
    }

    public boolean contains(@NotNull String id) {
        return blocksById.containsKey(id);
    }

    public int getBlockCount() {
        return blocksById.size();
    }

    public int getStateCount() {
        return allBlockStates.size();
    }

    public void freeze() {
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void clear() {
        if (frozen) {
            throw new IllegalStateException("Cannot clear frozen registry");
        }
        blocksById.clear();
        allBlockStates.clear();
    }

    @NotNull
    public List<String> getBlockIds() {
        return new ArrayList<>(blocksById.keySet());
    }
}
