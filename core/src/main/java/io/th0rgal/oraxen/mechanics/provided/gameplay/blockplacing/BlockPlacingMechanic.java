package io.th0rgal.oraxen.mechanics.provided.gameplay.blockplacing;

import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BlockPlacingMechanic extends Mechanic {

    private final int blockStateId;

    public BlockPlacingMechanic(MechanicFactory factory, ConfigurationSection config) {
        super(factory, config);
        this.blockStateId = config.getInt("block_state_id", 0);
    }

    public int getBlockStateId() {
        return blockStateId;
    }

    public String getMechanicID() {
        return getItemID() + "_block_placing";
    }
}
