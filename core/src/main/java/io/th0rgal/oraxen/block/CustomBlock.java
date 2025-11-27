package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface CustomBlock {

    @NotNull
    String getId();

    @NotNull
    ImmutableBlockState getDefaultState();

    @NotNull
    Collection<ImmutableBlockState> getStates();

    @NotNull
    Property<?>[] getProperties();

    @Nullable
    Property<?> getProperty(@NotNull String name);

    @NotNull
    ImmutableBlockState getStateFromProperties(@NotNull Map<Property<?>, Comparable<?>> properties);

    @NotNull
    ImmutableBlockState getStateById(int stateId);

    int getStateCount();

    @NotNull
    BlockSettings getSettings();
}
