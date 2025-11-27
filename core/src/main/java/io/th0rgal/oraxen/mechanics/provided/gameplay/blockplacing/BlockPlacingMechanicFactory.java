package io.th0rgal.oraxen.mechanics.provided.gameplay.blockplacing;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BlockPlacingMechanicFactory extends MechanicFactory {

    public BlockPlacingMechanicFactory(@NotNull ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new BlockPlacingListener());
    }

    @Override
    public @NotNull Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new BlockPlacingMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }
}
