package io.th0rgal.oraxen.block;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockManager {
    private static final String DEFAULT_NAMESPACE = "oraxen";

    private final BlockRegistry blockRegistry;
    private final IdAllocator internalIdAllocator;
    private final VisualBlockStateAllocator visualStateAllocator;
    private final int[] blockStateMappings;
    private final ImmutableBlockState[] allBlockStates;

    private volatile boolean initialized = false;

    public BlockManager() {
        this.blockRegistry = new BlockRegistry();
        int estimatedStateCount = 16384;
        this.internalIdAllocator = new IdAllocator(0);
        this.visualStateAllocator = new VisualBlockStateAllocator(estimatedStateCount);
        this.blockStateMappings = new int[estimatedStateCount];
        Arrays.fill(blockStateMappings, -1);
        this.allBlockStates = new ImmutableBlockState[estimatedStateCount];
    }

    public void registerBlock(@NotNull CustomBlock block) {
        if (initialized) {
            throw new IllegalStateException("Cannot register block after initialization");
        }
        blockRegistry.register(block);
    }

    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("BlockManager already initialized");
        }

        Logs.logInfo("Initializing block system...");

        try {
            blockRegistry.freeze();

            int stateIndex = 0;
            for (CustomBlock block : blockRegistry.getAllBlocks()) {
                for (ImmutableBlockState state : block.getStates()) {
                    if (stateIndex < allBlockStates.length) {
                        state.setInternalId(stateIndex);
                        allBlockStates[stateIndex] = state;
                        stateIndex++;
                    }
                }
            }

            internalIdAllocator.processPendingAllocations();
            visualStateAllocator.processPendingAllocations();

            ImmutableBlockState fallbackState = allBlockStates[0] != null ? allBlockStates[0] : null;
            if (fallbackState != null) {
                BlockRegistryMirror.init(allBlockStates, fallbackState);
            }

            initialized = true;
            Logs.logInfo(String.format("Block system initialized. Total blocks: %d, Total states: %d",
                    blockRegistry.getBlockCount(), stateIndex));
        } catch (Exception e) {
            Logs.logError("Failed to initialize block manager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Block manager initialization failed", e);
        }
    }

    @Nullable
    public CustomBlock getBlock(@NotNull String id) {
        return blockRegistry.getBlock(id);
    }

    @NotNull
    public Collection<CustomBlock> getAllBlocks() {
        return blockRegistry.getAllBlocks();
    }

    @NotNull
    public ImmutableBlockState getStateById(int stateId) {
        if (!BlockRegistryMirror.isInitialized()) {
            throw new IllegalStateException("BlockManager not initialized");
        }
        return BlockRegistryMirror.getById(stateId);
    }

    @NotNull
    public int[] getBlockStateMappings() {
        return blockStateMappings;
    }

    public void setBlockStateMapping(int internalId, int visualId) {
        if (internalId >= 0 && internalId < blockStateMappings.length) {
            blockStateMappings[internalId] = visualId;
        }
    }

    public int getBlockStateMapping(int internalId) {
        if (internalId < 0 || internalId >= blockStateMappings.length) {
            return -1;
        }
        return blockStateMappings[internalId];
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getTotalStateCount() {
        return allBlockStates.length;
    }

    public int getRegisteredStateCount() {
        int count = 0;
        for (ImmutableBlockState state : allBlockStates) {
            if (state != null) count++;
        }
        return count;
    }

    @NotNull
    public IdAllocator getInternalIdAllocator() {
        return internalIdAllocator;
    }

    @NotNull
    public VisualBlockStateAllocator getVisualStateAllocator() {
        return visualStateAllocator;
    }

    @NotNull
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    public static String createBlockKey(int index) {
        return DEFAULT_NAMESPACE + ":custom_" + index;
    }
}
