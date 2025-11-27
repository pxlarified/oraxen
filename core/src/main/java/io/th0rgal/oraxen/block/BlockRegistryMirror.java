package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockRegistryMirror {
    private static ImmutableBlockState[] blockStates;
    private static ImmutableBlockState fallbackState;

    public static void init(@NotNull ImmutableBlockState[] states, @NotNull ImmutableBlockState fallback) {
        if (blockStates != null) {
            throw new IllegalStateException("BlockRegistryMirror already initialized");
        }
        blockStates = states;
        fallbackState = fallback;
    }

    @NotNull
    public static ImmutableBlockState getById(int stateId) {
        if (blockStates == null) {
            throw new IllegalStateException("BlockRegistryMirror not initialized");
        }

        if (stateId < 0 || stateId >= blockStates.length) {
            return fallbackState;
        }

        return blockStates[stateId];
    }

    @NotNull
    public static ImmutableBlockState[] getAllStates() {
        if (blockStates == null) {
            throw new IllegalStateException("BlockRegistryMirror not initialized");
        }
        return blockStates;
    }

    public static int getStateCount() {
        return blockStates != null ? blockStates.length : 0;
    }

    public static void reset() {
        blockStates = null;
        fallbackState = null;
    }

    public static boolean isInitialized() {
        return blockStates != null;
    }
}
