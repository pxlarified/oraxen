package io.th0rgal.oraxen.nms.v1_20_R1;

import io.th0rgal.oraxen.block.CustomBlock;
import io.th0rgal.oraxen.block.CustomBlockShape;
import io.th0rgal.oraxen.nms.BlockHandler;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockHandlerImpl implements BlockHandler {

    private final Map<String, CustomBlock> blockRegistry = new ConcurrentHashMap<>();

    @Override
    public void registerCustomBlock(CustomBlock block) {
        blockRegistry.put(block.getId().toString(), block);
    }

    @Override
    public void unregisterCustomBlock(CustomBlock block) {
        blockRegistry.remove(block.getId().toString());
    }

    @Nullable
    @Override
    public CustomBlock getCustomBlock(Block block) {
        return null;
    }

    @Override
    public void injectBlockBehavior(CustomBlock block) {

    }

    @Nullable
    @Override
    public CustomBlockShape getCollisionShape(Block block, CustomBlock customBlock) {
        return null;
    }

    @Nullable
    @Override
    public CustomBlockShape getVisualShape(Block block, CustomBlock customBlock) {
        return null;
    }

    @Override
    public void updateBlockState(Block block, String propertyName, String value) {

    }

    @Override
    public void updateBlockState(Block block, Map<String, String> properties) {

    }
}
