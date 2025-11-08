package io.th0rgal.oraxen.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public abstract class BlockBehavior {

    public void onPlace(BlockPlaceEvent event, CustomBlock block) {
    }

    public void onBreak(BlockBreakEvent event, CustomBlock block) {
    }

    public void onTick(Block block, CustomBlock customBlock) {
    }

    public void onRandomTick(Block block, CustomBlock customBlock) {
    }

    public void onInteract(Block block, Player player, EquipmentSlot hand, CustomBlock customBlock) {
    }

    public void onEntityInside(Block block, Entity entity, CustomBlock customBlock) {
    }

    public void onNeighborUpdate(Block block, Block neighborBlock, CustomBlock customBlock) {
    }

    @Nullable
    public CustomBlockShape getCollisionShape(Block block, CustomBlock customBlock) {
        return null;
    }

    @Nullable
    public CustomBlockShape getVisualShape(Block block, CustomBlock customBlock) {
        return null;
    }
}
