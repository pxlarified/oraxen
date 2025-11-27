package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

public class SimpleCustomBlock extends AbstractCustomBlock {

    public SimpleCustomBlock(
            @NotNull String id,
            @NotNull Property<?>[] properties,
            @NotNull BlockSettings settings) {
        super(id, properties, settings);
    }
}
