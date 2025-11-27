package io.th0rgal.oraxen.block;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImmutableBlockState implements Comparable<ImmutableBlockState> {
    private final CustomBlock block;
    private final Map<Property<?>, Comparable<?>> propertyMap;
    private final int stateId;

    private BlockSettings settings;
    private int internalId = -1;
    private int visualId = -1;

    public ImmutableBlockState(
            @NotNull CustomBlock block,
            @NotNull Map<Property<?>, Comparable<?>> propertyMap,
            int stateId) {
        this.block = block;
        this.propertyMap = new Reference2ObjectArrayMap<>(propertyMap);
        this.stateId = stateId;
        this.settings = new BlockSettings();
    }

    @NotNull
    public CustomBlock getBlock() {
        return block;
    }

    @NotNull
    public Map<Property<?>, Comparable<?>> getPropertyMap() {
        return propertyMap;
    }

    public int getStateId() {
        return stateId;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public int getVisualId() {
        return visualId;
    }

    public void setVisualId(int visualId) {
        this.visualId = visualId;
    }

    @NotNull
    public BlockSettings getSettings() {
        return settings;
    }

    public void setSettings(@NotNull BlockSettings settings) {
        this.settings = settings;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Comparable<T>> T getValue(@NotNull Property<T> property) {
        return (T) propertyMap.get(property);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> ImmutableBlockState with(
            @NotNull Property<T> property,
            @NotNull T value) {
        if (propertyMap.containsKey(property) && propertyMap.get(property).equals(value)) {
            return this;
        }

        Map<Property<?>, Comparable<?>> newMap = new HashMap<>(propertyMap);
        newMap.put((Property<?>) property, (Comparable<?>) value);
        return block.getStateFromProperties(newMap);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public ImmutableBlockState withUnchecked(
            @NotNull Property<?> property,
            @NotNull Comparable<?> value) {
        if (propertyMap.containsKey(property) && propertyMap.get(property).equals(value)) {
            return this;
        }

        Map<Property<?>, Comparable<?>> newMap = new HashMap<>(propertyMap);
        newMap.put(property, value);
        return block.getStateFromProperties(newMap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(block.getId());

        if (!propertyMap.isEmpty()) {
            sb.append('[');
            boolean first = true;
            for (Map.Entry<Property<?>, Comparable<?>> entry : propertyMap.entrySet()) {
                if (!first) sb.append(',');
                sb.append(entry.getKey().getName()).append('=').append(entry.getValue());
                first = false;
            }
            sb.append(']');
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = block.getId().hashCode();
        result = 31 * result + propertyMap.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImmutableBlockState other)) return false;
        return block.getId().equals(other.block.getId())
                && propertyMap.equals(other.propertyMap);
    }

    @Override
    public int compareTo(@NotNull ImmutableBlockState o) {
        int blockCompare = block.getId().compareTo(o.block.getId());
        if (blockCompare != 0) return blockCompare;
        return Integer.compare(stateId, o.stateId);
    }
}
