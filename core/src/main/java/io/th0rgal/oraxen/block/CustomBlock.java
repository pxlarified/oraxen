package io.th0rgal.oraxen.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CustomBlock {

    @NotNull
    Key getId();

    @NotNull
    BlockSettings getSettings();

    @NotNull
    BlockProperties getProperties();

    @NotNull
    BlockAppearance getAppearance();

    @Nullable
    BlockBehavior getBehavior();
}
