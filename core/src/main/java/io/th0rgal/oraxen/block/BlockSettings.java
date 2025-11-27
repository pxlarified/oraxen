package io.th0rgal.oraxen.block;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class BlockSettings {
    private float hardness = 1.0f;
    private float resistance = 1.0f;
    private float friction = 0.6f;
    private float speedFactor = 1.0f;
    private float jumpFactor = 1.0f;
    private int luminance = 0;
    private boolean burnable = false;
    private boolean replaceable = false;
    private boolean requiresCorrectTool = false;
    private boolean randomTicks = false;
    private Material visualMaterial = Material.BARRIER;

    public BlockSettings() {
    }

    public static BlockSettings of(@NotNull Material material) {
        BlockSettings settings = new BlockSettings();
        settings.hardness = material.getHardness();
        return settings;
    }

    public float getHardness() {
        return hardness;
    }

    public void setHardness(float hardness) {
        this.hardness = hardness;
    }

    public float getResistance() {
        return resistance;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getSpeedFactor() {
        return speedFactor;
    }

    public void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    public float getJumpFactor() {
        return jumpFactor;
    }

    public void setJumpFactor(float jumpFactor) {
        this.jumpFactor = jumpFactor;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminance(int luminance) {
        this.luminance = luminance;
    }

    public boolean isBurnable() {
        return burnable;
    }

    public void setBurnable(boolean burnable) {
        this.burnable = burnable;
    }

    public boolean isReplaceable() {
        return replaceable;
    }

    public void setReplaceable(boolean replaceable) {
        this.replaceable = replaceable;
    }

    public boolean isRequiresCorrectTool() {
        return requiresCorrectTool;
    }

    public void setRequiresCorrectTool(boolean requiresCorrectTool) {
        this.requiresCorrectTool = requiresCorrectTool;
    }

    public boolean isRandomTicks() {
        return randomTicks;
    }

    public void setRandomTicks(boolean randomTicks) {
        this.randomTicks = randomTicks;
    }

    public Material getVisualMaterial() {
        return visualMaterial;
    }

    public void setVisualMaterial(Material visualMaterial) {
        this.visualMaterial = visualMaterial != null ? visualMaterial : Material.BARRIER;
    }
}
