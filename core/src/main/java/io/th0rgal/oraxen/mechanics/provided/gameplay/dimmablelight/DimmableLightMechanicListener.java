package io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockInteractEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockInteractEvent;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanic;
import io.th0rgal.oraxen.utils.BlockHelpers;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight.DimmableLightMechanic.DIMMABLE_LIGHT_LEVEL_KEY;

public class DimmableLightMechanicListener implements Listener {

    private final DimmableLightMechanicFactory factory;

    public DimmableLightMechanicListener(DimmableLightMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFurnitureInteract(OraxenFurnitureInteractEvent event) {
        Player player = event.getPlayer();
        FurnitureMechanic furnitureMechanic = event.getMechanic();
        Block block = event.getBlock();
        Entity baseEntity = event.getBaseEntity();

        Mechanic mechanic = factory.getMechanic(furnitureMechanic.getItemID());
        if (!(mechanic instanceof DimmableLightMechanic dimmableLight)) return;

        handleDimmableLight(player, block, baseEntity, dimmableLight, player.isSneaking());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNoteBlockInteract(OraxenNoteBlockInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        NoteBlockMechanic noteBlockMechanic = event.getMechanic();

        if (!ProtectionLib.canInteract(player, block.getLocation())) return;

        Mechanic mechanic = factory.getMechanic(noteBlockMechanic.getItemID());
        if (!(mechanic instanceof DimmableLightMechanic dimmableLight)) return;

        handleDimmableLight(player, block, null, dimmableLight, player.isSneaking());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStringBlockInteract(OraxenStringBlockInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        StringBlockMechanic stringBlockMechanic = event.getMechanic();

        if (!ProtectionLib.canInteract(player, block.getLocation())) return;

        Mechanic mechanic = factory.getMechanic(stringBlockMechanic.getItemID());
        if (!(mechanic instanceof DimmableLightMechanic dimmableLight)) return;

        handleDimmableLight(player, block, null, dimmableLight, player.isSneaking());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Skip noteblocks and stringblocks - they are handled by onNoteBlockInteract and onStringBlockInteract
        // Note: Noteblocks fire OraxenNoteBlockInteractEvent for both left and right clicks
        // Stringblocks only fire OraxenStringBlockInteractEvent for right clicks, so left clicks need handling here
        if (block.getType() == Material.NOTE_BLOCK) {
            // Noteblocks are handled by onNoteBlockInteract via OraxenNoteBlockInteractEvent
            return;
        }

        // Check furniture (furniture doesn't fire custom events for left clicks, so handle here)
        FurnitureMechanic furnitureMechanic = OraxenFurniture.getFurnitureMechanic(block);
        if (furnitureMechanic != null) {
            Entity baseEntity = furnitureMechanic.getBaseEntity(block);
            Mechanic mechanic = factory.getMechanic(furnitureMechanic.getItemID());
            if (mechanic instanceof DimmableLightMechanic dimmableLight) {
                handleDimmableLight(player, block, baseEntity, dimmableLight, player.isSneaking());
                return;
            }
        }

        // Check stringblock (only for left clicks, right clicks are handled by onStringBlockInteract)
        if (block.getType() == Material.TRIPWIRE) {
            StringBlockMechanic stringBlockMechanic = OraxenBlocks.getStringMechanic(block);
            if (stringBlockMechanic != null) {
            Mechanic mechanic = factory.getMechanic(stringBlockMechanic.getItemID());
            if (mechanic instanceof DimmableLightMechanic dimmableLight) {
                    if (!ProtectionLib.canInteract(player, block.getLocation())) return;
                    handleDimmableLight(player, block, null, dimmableLight, player.isSneaking());
                }
            }
        }
    }

    private void handleDimmableLight(Player player, Block block, Entity entity, DimmableLightMechanic dimmableLight, boolean isSneaking) {
        // Prefer storing state on the entity so the light level is shared across all barriers of a furniture piece
        PersistentDataContainer pdc = entity != null ? entity.getPersistentDataContainer()
                                                     : block != null ? BlockHelpers.getPDC(block) : null;
        if (pdc == null) return;

        Block targetBlock = entity != null && entity.getLocation().getBlock() != null
            ? entity.getLocation().getBlock()
            : block;

        int currentLevel = pdc.getOrDefault(DIMMABLE_LIGHT_LEVEL_KEY, PersistentDataType.INTEGER, dimmableLight.getDefaultLightLevel());
        
        int newLevel;
        if (isSneaking) {
            // Shift+click: decrease brightness
            newLevel = Math.max(dimmableLight.getMinLight(), currentLevel - dimmableLight.getStepSize());
        } else {
            // Left click: increase brightness
            newLevel = Math.min(dimmableLight.getMaxLight(), currentLevel + dimmableLight.getStepSize());
        }

        if (newLevel == currentLevel) return;

        pdc.set(DIMMABLE_LIGHT_LEVEL_KEY, PersistentDataType.INTEGER, newLevel);
        updateLightLevel(targetBlock, entity, newLevel);
    }

    private void updateLightLevel(Block block, Entity entity, int lightLevel) {
        if (block == null) return;

        // Remove old light blocks
        removeLightBlocks(block);

        // Create new light blocks with the new level
        if (lightLevel > 0) {
            createLightBlocks(block, lightLevel);
        }

        // For furniture with barriers, update all barrier blocks
        if (entity != null) {
            FurnitureMechanic furnitureMechanic = OraxenFurniture.getFurnitureMechanic(entity);
            if (furnitureMechanic != null && furnitureMechanic.hasBarriers(entity)) {
                Location baseLocation = block.getLocation();
                for (io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.BlockLocation barrierLoc : furnitureMechanic.getBarriers(entity)) {
                    Block barrierBlock = barrierLoc.add(baseLocation).getBlock();
                    if (barrierBlock != null) {
                        removeLightBlocks(barrierBlock);
                        if (lightLevel > 0) {
                            createLightBlocks(barrierBlock, lightLevel);
                        }
                    }
                }
            }
        }
    }

    private void removeLightBlocks(Block block) {
        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.SELF};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.LIGHT) {
                relative.setType(Material.AIR);
            }
        }
    }

    private void createLightBlocks(Block block, int lightLevel) {
        Light lightData = (Light) Material.LIGHT.createBlockData();
        lightData.setLevel(lightLevel);

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.SELF};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType().isAir() || relative.getType() == Material.LIGHT) {
                if (relative.getBlockData() instanceof Light existingLight && existingLight.getLevel() > lightLevel) {
                    continue;
                }
                relative.setBlockData(lightData);
            }
        }
    }
}

