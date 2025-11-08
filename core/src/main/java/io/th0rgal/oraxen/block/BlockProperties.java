package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockProperties {

    private final Map<String, BlockProperty<?>> properties;

    public BlockProperties() {
        this.properties = new LinkedHashMap<>();
    }

    public void addProperty(@NotNull BlockProperty<?> property) {
        properties.put(property.getName(), property);
    }

    @Nullable
    public BlockProperty<?> getProperty(@NotNull String name) {
        return properties.get(name);
    }

    @NotNull
    public Collection<BlockProperty<?>> getAllProperties() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @NotNull
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public boolean hasProperty(@NotNull String name) {
        return properties.containsKey(name);
    }

    public int getPropertyCount() {
        return properties.size();
    }

    @NotNull
    public Map<String, BlockProperty<?>> asMap() {
        return Collections.unmodifiableMap(properties);
    }

    @NotNull
    public Map<String, BlockProperty<?>> getProperties() {
        return asMap();
    }
}
