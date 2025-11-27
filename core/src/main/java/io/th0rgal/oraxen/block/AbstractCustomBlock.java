package io.th0rgal.oraxen.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractCustomBlock implements CustomBlock {
    private final String id;
    private final Property<?>[] properties;
    private final Map<String, Property<?>> propertyMap;
    private final ImmutableBlockState[] states;
    private final ImmutableBlockState defaultState;
    private final BlockSettings settings;

    public AbstractCustomBlock(
            @NotNull String id,
            @NotNull Property<?>[] properties,
            @NotNull BlockSettings settings) {
        this.id = id;
        this.properties = properties;
        this.settings = settings;
        this.propertyMap = new Object2ObjectArrayMap<>();

        for (Property<?> property : properties) {
            propertyMap.put(property.getName(), property);
        }

        this.states = generateStates();
        this.defaultState = states[0];
    }

    @Override
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    @NotNull
    public ImmutableBlockState getDefaultState() {
        return defaultState;
    }

    @Override
    @NotNull
    public Collection<ImmutableBlockState> getStates() {
        return Arrays.asList(states);
    }

    @Override
    @NotNull
    public Property<?>[] getProperties() {
        return properties;
    }

    @Override
    @Nullable
    public Property<?> getProperty(@NotNull String name) {
        return propertyMap.get(name);
    }

    @Override
    @NotNull
    public ImmutableBlockState getStateFromProperties(@NotNull Map<Property<?>, Comparable<?>> properties) {
        ImmutableBlockState state = defaultState;
        for (Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
            state = withProperty(state, entry.getKey(), entry.getValue());
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private ImmutableBlockState withProperty(
            ImmutableBlockState state,
            Property<?> property,
            Comparable<?> value) {
        return state.withUnchecked(property, value);
    }

    @Override
    @NotNull
    public ImmutableBlockState getStateById(int stateId) {
        if (stateId < 0 || stateId >= states.length) {
            return defaultState;
        }
        return states[stateId];
    }

    @Override
    public int getStateCount() {
        return states.length;
    }

    @Override
    @NotNull
    public BlockSettings getSettings() {
        return settings;
    }

    protected ImmutableBlockState[] generateStates() {
        List<ImmutableBlockState> stateList = new ArrayList<>();
        generateStateVariants(stateList, 0, new HashMap<>());
        return stateList.toArray(new ImmutableBlockState[0]);
    }

    private void generateStateVariants(
            @NotNull List<ImmutableBlockState> stateList,
            int propertyIndex,
            @NotNull Map<Property<?>, Comparable<?>> currentMap) {
        if (propertyIndex == properties.length) {
            int stateId = stateList.size();
            ImmutableBlockState state = new ImmutableBlockState(this, new HashMap<>(currentMap), stateId);
            stateList.add(state);
            return;
        }

        Property<?> property = properties[propertyIndex];
        for (Comparable<?> value : property.getPossibleValues()) {
            currentMap.put(property, value);
            generateStateVariants(stateList, propertyIndex + 1, currentMap);
        }
        currentMap.remove(property);
    }

    protected void registerState(@NotNull ImmutableBlockState state, int internalId, int visualId) {
        state.setInternalId(internalId);
        state.setVisualId(visualId);
    }
}
