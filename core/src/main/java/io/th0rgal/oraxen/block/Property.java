package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Property<T extends Comparable<T>> {
    private final String name;
    private final Class<T> valueClass;

    public Property(@NotNull String name, @NotNull Class<T> valueClass) {
        this.name = name;
        this.valueClass = valueClass;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Class<T> getValueClass() {
        return valueClass;
    }

    @NotNull
    public abstract T getDefaultValue();

    @NotNull
    public abstract T[] getPossibleValues();

    @Nullable
    public T parseValue(@NotNull String value) {
        for (T possible : getPossibleValues()) {
            if (possible.toString().equalsIgnoreCase(value)) {
                return possible;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Property<?> other)) return false;
        return name.equals(other.name);
    }

    public static class BooleanProperty extends Property<Boolean> {
        private static final Boolean[] VALUES = {true, false};

        public BooleanProperty(@NotNull String name) {
            super(name, Boolean.class);
        }

        @Override
        public Boolean getDefaultValue() {
            return false;
        }

        @Override
        public Boolean[] getPossibleValues() {
            return VALUES;
        }
    }

    public static class IntProperty extends Property<Integer> {
        private final int min;
        private final int max;

        public IntProperty(@NotNull String name, int min, int max) {
            super(name, Integer.class);
            this.min = min;
            this.max = max;
        }

        @Override
        public Integer getDefaultValue() {
            return min;
        }

        @Override
        public Integer[] getPossibleValues() {
            Integer[] values = new Integer[max - min + 1];
            for (int i = 0; i < values.length; i++) {
                values[i] = min + i;
            }
            return values;
        }

        @Override
        public Integer parseValue(String value) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
            } catch (NumberFormatException e) {
                // Ignored
            }
            return null;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public static class DirectionProperty extends Property<Direction> {
        private final Direction[] values;

        public DirectionProperty(@NotNull String name, @NotNull Direction... values) {
            super(name, Direction.class);
            this.values = values;
        }

        @Override
        public Direction getDefaultValue() {
            return values[0];
        }

        @Override
        public Direction[] getPossibleValues() {
            return values;
        }
    }

    public enum Direction {
        NORTH, SOUTH, EAST, WEST, UP, DOWN;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static class EnumProperty<E extends Enum<E>> extends Property<E> {
        private final E[] values;

        @SafeVarargs
        public EnumProperty(@NotNull String name, @NotNull E... values) {
            super(name, (Class<E>) values[0].getClass());
            this.values = values;
        }

        @Override
        public E getDefaultValue() {
            return values[0];
        }

        @Override
        public E[] getPossibleValues() {
            return values;
        }
    }
}
