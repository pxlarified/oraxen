package io.th0rgal.oraxen.api;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.block.BlockRegistry;
import io.th0rgal.oraxen.block.CustomBlock;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class OraxenCustomBlocks {

    @Nullable
    public static CustomBlock getBlockById(@NotNull String id) {
        return getRegistry().getBlock(id);
    }

    @Nullable
    public static CustomBlock getBlockById(@NotNull Key key) {
        return getRegistry().getBlock(key);
    }

    @NotNull
    public static Optional<CustomBlock> getBlockOptional(@NotNull String id) {
        return getRegistry().getBlockOptional(id);
    }

    @NotNull
    public static Optional<CustomBlock> getBlockOptional(@NotNull Key key) {
        return getRegistry().getBlockOptional(key);
    }

    public static boolean exists(@NotNull String id) {
        return getRegistry().isRegistered(id);
    }

    public static boolean exists(@NotNull Key key) {
        return getRegistry().isRegistered(key);
    }

    @NotNull
    public static Collection<CustomBlock> getLoadedBlocks() {
        return getRegistry().getLoadedBlocks();
    }

    @NotNull
    public static List<String> getBlockNames() {
        return getRegistry().getBlockNames();
    }

    public static int getBlockCount() {
        return getRegistry().getBlockCount();
    }

    @NotNull
    private static BlockRegistry getRegistry() {
        return OraxenPlugin.get().getBlockManager().getRegistry();
    }
}
