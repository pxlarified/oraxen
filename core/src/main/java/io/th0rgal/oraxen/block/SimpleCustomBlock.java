package io.th0rgal.oraxen.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleCustomBlock implements CustomBlock {

    private final Key id;
    private final BlockSettings settings;
    private final BlockProperties properties;
    private final BlockAppearance appearance;
    private final BlockBehavior behavior;

    public SimpleCustomBlock(
            @NotNull Key id,
            @NotNull BlockSettings settings,
            @NotNull BlockProperties properties,
            @NotNull BlockAppearance appearance,
            @Nullable BlockBehavior behavior
    ) {
        this.id = id;
        this.settings = settings;
        this.properties = properties;
        this.appearance = appearance;
        this.behavior = behavior;
    }

    @Override
    @NotNull
    public Key getId() {
        return id;
    }

    @Override
    @NotNull
    public BlockSettings getSettings() {
        return settings;
    }

    @Override
    @NotNull
    public BlockProperties getProperties() {
        return properties;
    }

    @Override
    @NotNull
    public BlockAppearance getAppearance() {
        return appearance;
    }

    @Override
    @Nullable
    public BlockBehavior getBehavior() {
        return behavior;
    }

    @Override
    public String toString() {
        return "SimpleCustomBlock{" +
                "id=" + id +
                ", properties=" + properties.getPropertyCount() +
                '}';
    }
}
