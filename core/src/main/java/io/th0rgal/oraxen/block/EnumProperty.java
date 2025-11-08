package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnumProperty implements BlockProperty<String> {

    private final String name;
    private final List<String> values;
    private final String defaultValue;

    public EnumProperty(@NotNull String name, @NotNull List<String> values) {
        this(name, values, values.isEmpty() ? "" : values.get(0));
    }

    public EnumProperty(@NotNull String name, @NotNull List<String> values, @NotNull String defaultValue) {
        this.name = name;
        this.values = List.copyOf(values);
        this.defaultValue = defaultValue;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public List<String> getPossibleValues() {
        return values;
    }

    @Override
    @NotNull
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    @NotNull
    public String valueToString(String value) {
        return value;
    }

    @Override
    @NotNull
    public String stringToValue(@NotNull String value) {
        return isValidValue(value) ? value : defaultValue;
    }

    @Override
    public boolean isValidValue(String value) {
        return values.contains(value);
    }
}
