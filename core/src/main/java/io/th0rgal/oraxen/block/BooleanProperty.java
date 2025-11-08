package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanProperty implements BlockProperty<Boolean> {

    private final String name;

    public BooleanProperty(@NotNull String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public List<Boolean> getPossibleValues() {
        return List.of(false, true);
    }

    @Override
    @NotNull
    public Boolean getDefaultValue() {
        return false;
    }

    @Override
    @NotNull
    public String valueToString(Boolean value) {
        return String.valueOf(value);
    }

    @Override
    @NotNull
    public Boolean stringToValue(@NotNull String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean isValidValue(Boolean value) {
        return true;
    }
}
