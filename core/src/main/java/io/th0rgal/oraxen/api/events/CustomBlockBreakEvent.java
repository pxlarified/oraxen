package io.th0rgal.oraxen.api.events;

import io.th0rgal.oraxen.block.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class CustomBlockBreakEvent extends Event {

    private final CustomBlock customBlock;
    private final Block block;
    private final Player player;
    private final BlockBreakEvent bukkitEvent;
    private boolean cancelled = false;

    public CustomBlockBreakEvent(CustomBlock customBlock, Block block, Player player, BlockBreakEvent bukkitEvent) {
        this.customBlock = customBlock;
        this.block = block;
        this.player = player;
        this.bukkitEvent = bukkitEvent;
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
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public BlockBreakEvent getBukkitEvent() {
        return bukkitEvent;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
        bukkitEvent.setCancelled(cancel);
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
