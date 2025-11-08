package io.th0rgal.oraxen.api.events;

import io.th0rgal.oraxen.block.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomBlockStateUpdateEvent extends Event {

    private final CustomBlock customBlock;
    private final Block block;
    private final Map<String, String> previousState;
    private final Map<String, String> newState;
    private boolean cancelled = false;

    public CustomBlockStateUpdateEvent(CustomBlock customBlock, Block block, Map<String, String> previousState, Map<String, String> newState) {
        this.customBlock = customBlock;
        this.block = block;
        this.previousState = previousState;
        this.newState = newState;
    }

    @NotNull
    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    @NotNull
    public Map<String, String> getPreviousState() {
        return previousState;
    }

    @NotNull
    public Map<String, String> getNewState() {
        return newState;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
