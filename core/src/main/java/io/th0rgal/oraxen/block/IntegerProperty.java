package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IntegerProperty implements BlockProperty<Integer> {

    private final String name;
    private final int minValue;
    private final int maxValue;
    private final int defaultValue;
    private final List<Integer> values;

    public IntegerProperty(@NotNull String name, int minValue, int maxValue) {
        this(name, minValue, maxValue, minValue);
    }

    public IntegerProperty(@NotNull String name, int minValue, int maxValue, int defaultValue) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = Math.max(minValue, Math.min(maxValue, defaultValue));
        
        List<Integer> vals = new ArrayList<>();
        for (int i = minValue; i <= maxValue; i++) {
            vals.add(i);
        }
        this.values = List.copyOf(vals);
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public List<Integer> getPossibleValues() {
        return values;
    }

    @Override
    @NotNull
    public Integer getDefaultValue() {
        return defaultValue;
    }

    @Override
    @NotNull
    public String valueToString(Integer value) {
        return String.valueOf(value);
    }

    @Override
    @NotNull
    public Integer stringToValue(@NotNull String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isValidValue(Integer value) {
        return value >= minValue && value <= maxValue;
    }
}
