package io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;

public class DimmableLightMechanicFactory extends MechanicFactory {

    private static DimmableLightMechanicFactory instance;

    public DimmableLightMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(),
                new DimmableLightMechanicListener(this),
                new DimmableLightRefreshListener(this));
        instance = this;
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new DimmableLightMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }

    public static DimmableLightMechanicFactory getInstance() {
        return instance;
    }
}

