package io.th0rgal.oraxen.block;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class BlockSettings {

    private final Material material;
    private final float hardness;
    private final float resistance;
    private final boolean replaceable;
    private final boolean requiresCorrectToolForDrops;
    private final int emitLight;
    private final String soundType;

    public BlockSettings(Material material, float hardness, float resistance, boolean replaceable, 
                        boolean requiresCorrectToolForDrops, int emitLight, String soundType) {
        this.material = material;
        this.hardness = hardness;
        this.resistance = resistance;
        this.replaceable = replaceable;
        this.requiresCorrectToolForDrops = requiresCorrectToolForDrops;
        this.emitLight = Math.max(0, Math.min(15, emitLight));
        this.soundType = soundType;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public float getHardness() {
        return hardness;
    }

    public float getResistance() {
        return resistance;
    }

    public boolean isReplaceable() {
        return replaceable;
    }

    public boolean requiresCorrectToolForDrops() {
        return requiresCorrectToolForDrops;
    }

    public int getEmitLight() {
        return emitLight;
    }

    @NotNull
    public String getSoundType() {
        return soundType;
    }

    public static class Builder {
        private Material material = Material.PAPER;
        private float hardness = 1.0f;
        private float resistance = 1.0f;
        private boolean replaceable = false;
        private boolean requiresCorrectToolForDrops = false;
        private int emitLight = 0;
        private String soundType = "wood";

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Builder resistance(float resistance) {
            this.resistance = resistance;
            return this;
        }

        public Builder replaceable(boolean replaceable) {
            this.replaceable = replaceable;
            return this;
        }

        public Builder requiresCorrectToolForDrops(boolean requiresCorrectToolForDrops) {
            this.requiresCorrectToolForDrops = requiresCorrectToolForDrops;
            return this;
        }

        public Builder emitLight(int emitLight) {
            this.emitLight = emitLight;
            return this;
        }

        public Builder soundType(String soundType) {
            this.soundType = soundType;
            return this;
        }

        public BlockSettings build() {
            return new BlockSettings(material, hardness, resistance, replaceable, requiresCorrectToolForDrops, emitLight, soundType);
        }
    }
}
