package io.th0rgal.oraxen.nms;

import io.th0rgal.oraxen.block.CustomBlock;
import io.th0rgal.oraxen.block.CustomBlockShape;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface BlockHandler {

    void registerCustomBlock(CustomBlock block);

    void unregisterCustomBlock(CustomBlock block);

    @Nullable
    CustomBlock getCustomBlock(Block block);

    void injectBlockBehavior(CustomBlock block);

    @Nullable
    CustomBlockShape getCollisionShape(Block block, CustomBlock customBlock);

    @Nullable
    CustomBlockShape getVisualShape(Block block, CustomBlock customBlock);

    void updateBlockState(Block block, String propertyName, String value);

    void updateBlockState(Block block, java.util.Map<String, String> properties);

    class EmptyBlockHandler implements BlockHandler {

        @Override
        public void registerCustomBlock(CustomBlock block) {
        }

        @Override
        public void unregisterCustomBlock(CustomBlock block) {
        }

        @Override
        public CustomBlock getCustomBlock(Block block) {
            return null;
        }

        @Override
        public void injectBlockBehavior(CustomBlock block) {
        }

        @Override
        public CustomBlockShape getCollisionShape(Block block, CustomBlock customBlock) {
            return null;
        }

        @Override
        public CustomBlockShape getVisualShape(Block block, CustomBlock customBlock) {
            return null;
        }

        @Override
        public void updateBlockState(Block block, String propertyName, String value) {
        }

        @Override
        public void updateBlockState(Block block, java.util.Map<String, String> properties) {
        }
    }
}
