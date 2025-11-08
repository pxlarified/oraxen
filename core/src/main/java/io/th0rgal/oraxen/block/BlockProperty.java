package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BlockProperty<T> {

    @NotNull
    String getName();

    @NotNull
    List<T> getPossibleValues();

    @NotNull
    T getDefaultValue();

    @NotNull
    String valueToString(T value);

    @NotNull
    T stringToValue(@NotNull String value);

    boolean isValidValue(T value);
}
