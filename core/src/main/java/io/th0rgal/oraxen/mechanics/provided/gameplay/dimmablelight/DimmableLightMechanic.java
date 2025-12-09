package io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.light.LightMechanic;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class DimmableLightMechanic extends Mechanic {

    public static final NamespacedKey DIMMABLE_LIGHT_LEVEL_KEY = new NamespacedKey(OraxenPlugin.get(), "dimmable_light_level");

    private final int minLight;
    private final int maxLight;
    private final int stepSize;
    private final LightMechanic baseLight;

    public DimmableLightMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        
        minLight = Math.max(0, Math.min(15, section.getInt("min_light", 0)));
        maxLight = Math.max(0, Math.min(15, section.getInt("max_light", 15)));
        stepSize = Math.max(1, section.getInt("step_size", 1));
        
        // Use base light level from light mechanic if available, otherwise use minLight
        int baseLightLevel = section.getInt("light", minLight);
        baseLight = new LightMechanic(section);
    }

    public int getMinLight() {
        return minLight;
    }

    public int getMaxLight() {
        return maxLight;
    }

    public int getStepSize() {
        return stepSize;
    }

    public LightMechanic getBaseLight() {
        return baseLight;
    }

    public boolean hasDimmableLight() {
        return true;
    }

    public int getDefaultLightLevel() {
        return baseLight.hasLightLevel() ? baseLight.getLightLevel() : minLight;
    }
}

